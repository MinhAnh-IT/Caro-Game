package com.vn.caro_game.services.impl;

import com.vn.caro_game.dtos.response.GameReplayResponse;
import com.vn.caro_game.dtos.response.GameStatisticsResponse;
import com.vn.caro_game.dtos.response.SimpleMoveResponse;
import com.vn.caro_game.dtos.response.GameHistorySummaryResponse;
import com.vn.caro_game.entities.*;
import com.vn.caro_game.enums.StatusCode;
import com.vn.caro_game.exceptions.CustomException;
import com.vn.caro_game.repositories.*;
import com.vn.caro_game.services.interfaces.GameStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of GameStatisticsService for managing game statistics and history
 * 
 * @author Caro Game Team
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GameStatisticsServiceImpl implements GameStatisticsService {

    private final GameHistoryRepository gameHistoryRepository;
    private final GameMatchRepository gameMatchRepository;
    private final MoveRepository moveRepository;
    private final UserRepository userRepository;
    private final GameRoomRepository gameRoomRepository;

    @Override
    @Transactional(readOnly = true)
    public GameStatisticsResponse getUserGameStatistics(Long userId) {
        log.info("Getting game statistics for user {}", userId);

        // Validate user exists
        getUserById(userId);

        // Get basic statistics
        Long totalWins = gameHistoryRepository.getWinCountByUserId(userId);
        Long totalLosses = gameHistoryRepository.getLossCountByUserId(userId);
        Long totalDraws = calculateDrawCount(userId);
        Long totalGames = totalWins + totalLosses + totalDraws;

        // Calculate percentages
        Double winRate = totalGames > 0 ? (totalWins.doubleValue() / totalGames) * 100 : 0.0;
        Double lossRate = totalGames > 0 ? (totalLosses.doubleValue() / totalGames) * 100 : 0.0;
        Double drawRate = totalGames > 0 ? (totalDraws.doubleValue() / totalGames) * 100 : 0.0;

        // Get game duration statistics
        List<GameHistory> userGames = gameHistoryRepository.findByUserIdList(userId);
        GameTimeStatistics timeStats = calculateGameTimeStatistics(userGames);

        // Calculate streaks
        StreakStatistics streakStats = calculateStreakStatistics(userId);

        // Build response
        GameStatisticsResponse response = new GameStatisticsResponse();
        response.setUserId(userId);
        response.setTotalGamesPlayed(totalGames);
        response.setTotalWins(totalWins);
        response.setTotalLosses(totalLosses);
        response.setTotalDraws(totalDraws);
        response.setWinRate(Math.round(winRate * 100.0) / 100.0);
        response.setLossRate(Math.round(lossRate * 100.0) / 100.0);
        response.setDrawRate(Math.round(drawRate * 100.0) / 100.0);
        response.setTotalGameTimeMinutes(timeStats.totalTimeMinutes);
        response.setAverageGameDurationMinutes(timeStats.averageDurationMinutes);
        response.setLongestGameDurationMinutes(timeStats.longestDurationMinutes);
        response.setShortestGameDurationMinutes(timeStats.shortestDurationMinutes);
        response.setCurrentWinStreak(streakStats.currentWinStreak);
        response.setBestWinStreak(streakStats.bestWinStreak);
        response.setPlayerRank(calculatePlayerRank(winRate, totalGames));
        response.setTotalScore(calculateTotalScore(totalWins, totalDraws, totalGames));

        log.info("Game statistics calculated for user {} - {} games played, {} wins", 
                userId, totalGames, totalWins);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public GameReplayResponse getGameReplay(Long gameId, Long userId) {
        log.info("Getting game replay for history {} by user {}", gameId, userId);

        // Get game history
        GameHistory gameHistory = gameHistoryRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(StatusCode.NOT_FOUND));

        // Validate user has access to this game
        if (!userId.equals(gameHistory.getWinnerId()) && !userId.equals(gameHistory.getLoserId())) {
            throw new CustomException(StatusCode.FORBIDDEN);
        }

        // Get game room for additional info
        GameRoom room = gameRoomRepository.findById(gameHistory.getRoomId())
                .orElseThrow(() -> new CustomException(StatusCode.ROOM_NOT_FOUND));

        // Find the game match
        List<GameMatch> matches = gameMatchRepository.findByRoomId(gameHistory.getRoomId());
        if (matches.isEmpty()) {
            throw new CustomException(StatusCode.NOT_FOUND);
        }

        GameMatch gameMatch = matches.get(matches.size() - 1); // Get the latest match

        // Get all moves for this match
        List<Move> moves = moveRepository.findByMatchIdOrderByMoveNumberAsc(gameMatch.getId());

        // Build detailed move responses
        List<SimpleMoveResponse> moveDetails = buildSimpleMoveList(moves, gameMatch);

        // Calculate final board state
        int[][] finalBoard = reconstructBoardFromMoves(moves, gameMatch);

        // Determine game result
        String gameResult = determineGameResult(gameHistory, gameMatch);

        // Build player info
        GameReplayResponse.PlayerInfo playerXInfo = buildPlayerInfo(gameMatch.getPlayerX(), "X", moves);
        GameReplayResponse.PlayerInfo playerOInfo = buildPlayerInfo(gameMatch.getPlayerO(), "O", moves);

        // Calculate game duration
        Long duration = calculateGameDuration(gameHistory.getGameStartedAt(), gameHistory.getGameEndedAt());

        // Build response
        GameReplayResponse response = new GameReplayResponse();
        response.setGameId(gameId);
        response.setRoomId(gameHistory.getRoomId());
        response.setRoomName(room.getName());
        response.setPlayerX(playerXInfo);
        response.setPlayerO(playerOInfo);
        response.setWinnerId(gameHistory.getWinnerId());
        response.setEndReason(gameHistory.getEndReason());
        response.setGameStartTime(gameHistory.getGameStartedAt());
        response.setGameEndTime(gameHistory.getGameEndedAt());
        response.setGameDurationMinutes(duration);
        response.setTotalMoves(moves.size());
        response.setMoves(moveDetails);
        response.setFinalBoardState(finalBoard);
        response.setGameResult(gameResult);
        response.setIsUserWinner(userId.equals(gameHistory.getWinnerId()));

        log.info("Game replay built for history {} - {} moves, {} minutes duration", 
                gameId, moves.size(), duration);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameHistorySummaryResponse> getUserGameReplays(Long userId, Pageable pageable) {
        log.info("Getting game replays for user {} with pagination", userId);

        // Get user game history with pagination
        Page<GameHistory> historyPage = gameHistoryRepository.findByUserId(userId, pageable);

        // Convert to replay responses (simplified version without moves)
        List<GameHistorySummaryResponse> replays = historyPage.getContent().stream()
                .map(history -> buildGameHistorySummary(history, userId))
                .collect(Collectors.toList());

        return new PageImpl<>(replays, pageable, historyPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameStatisticsResponse> getTopPlayersByWinRate(int limit, Pageable pageable) {
        log.info("Getting top {} players by win rate", limit);

        // Get all users with games
        List<User> usersWithGames = userRepository.findUsersWithGameHistory();
        
        // Calculate statistics for each user and sort by win rate
        List<GameStatisticsResponse> topPlayers = usersWithGames.stream()
                .map(user -> getUserGameStatistics(user.getId()))
                .filter(stats -> stats.getTotalGamesPlayed() >= 5) // Minimum 5 games
                .sorted((a, b) -> Double.compare(b.getWinRate(), a.getWinRate()))
                .limit(limit)
                .collect(Collectors.toList());

        return new PageImpl<>(topPlayers, pageable, topPlayers.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUserRanking(Long userId) {
        log.info("Getting ranking for user {}", userId);

        // Get user's win rate
        GameStatisticsResponse userStats = getUserGameStatistics(userId);
        
        if (userStats.getTotalGamesPlayed() < 5) {
            return null; // Not enough games to rank
        }

        // Count users with better win rate
        List<User> allUsers = userRepository.findUsersWithGameHistory();
        long betterUsers = allUsers.stream()
                .map(user -> getUserGameStatistics(user.getId()))
                .filter(stats -> stats.getTotalGamesPlayed() >= 5)
                .filter(stats -> stats.getWinRate() > userStats.getWinRate())
                .count();

        return betterUsers + 1; // Ranking starts from 1
    }

    // Helper methods

    /**
     * Gets user by ID or throws exception if not found
     */
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(StatusCode.USER_NOT_FOUND));
    }

    /**
     * Calculates draw count for a user
     */
    private Long calculateDrawCount(Long userId) {
        // Count games where user participated but there's no winner (draw)
        return gameHistoryRepository.findByUserIdList(userId).stream()
                .filter(history -> history.getWinnerId() == null && history.getLoserId() == null)
                .count();
    }

    /**
     * Calculates game time statistics
     */
    private GameTimeStatistics calculateGameTimeStatistics(List<GameHistory> games) {
        if (games.isEmpty()) {
            return new GameTimeStatistics(0L, 0.0, 0L, 0L);
        }

        List<Long> durations = games.stream()
                .filter(game -> game.getGameStartedAt() != null && game.getGameEndedAt() != null)
                .map(game -> Duration.between(game.getGameStartedAt(), game.getGameEndedAt()).toMinutes())
                .collect(Collectors.toList());

        if (durations.isEmpty()) {
            return new GameTimeStatistics(0L, 0.0, 0L, 0L);
        }

        Long totalTime = durations.stream().mapToLong(Long::longValue).sum();
        Double averageTime = durations.stream().mapToLong(Long::longValue).average().orElse(0.0);
        Long longestTime = durations.stream().mapToLong(Long::longValue).max().orElse(0L);
        Long shortestTime = durations.stream().mapToLong(Long::longValue).min().orElse(0L);

        return new GameTimeStatistics(totalTime, averageTime, longestTime, shortestTime);
    }

    /**
     * Calculates win streak statistics
     */
    private StreakStatistics calculateStreakStatistics(Long userId) {
        List<GameHistory> recentGames = gameHistoryRepository.findByUserIdList(userId);
        
        int currentStreak = 0;
        int bestStreak = 0;
        int tempStreak = 0;

        // Calculate current win streak (from most recent games)
        for (int i = 0; i < recentGames.size(); i++) {
            GameHistory game = recentGames.get(i);
            if (userId.equals(game.getWinnerId())) {
                if (i == 0) currentStreak++; // Only count if it's the most recent games
                tempStreak++;
                bestStreak = Math.max(bestStreak, tempStreak);
            } else {
                if (i == 0) currentStreak = 0; // Reset current streak if latest game was not a win
                tempStreak = 0;
            }
        }

        return new StreakStatistics(currentStreak, bestStreak);
    }

    /**
     * Calculates player rank based on win rate and games played
     */
    private String calculatePlayerRank(Double winRate, Long totalGames) {
        if (totalGames < 5) return "Unranked";
        if (winRate >= 80) return "Master";
        if (winRate >= 70) return "Expert";
        if (winRate >= 60) return "Advanced";
        if (winRate >= 50) return "Intermediate";
        if (winRate >= 40) return "Novice";
        return "Beginner";
    }

    /**
     * Calculates total score based on wins, draws and games
     */
    private Long calculateTotalScore(Long wins, Long draws, Long totalGames) {
        return wins * 3 + draws * 1 + totalGames; // 3 points per win, 1 per draw, 1 per game
    }

    /**
     * Builds simple move list for replay (optimized version)
     */
    private List<SimpleMoveResponse> buildSimpleMoveList(List<Move> moves, GameMatch gameMatch) {
        List<SimpleMoveResponse> simpleMoves = new ArrayList<>();

        for (Move move : moves) {
            SimpleMoveResponse simpleMove = new SimpleMoveResponse();
            simpleMove.setMoveId(move.getId());
            simpleMove.setPlayerId(move.getPlayer().getId());
            simpleMove.setPlayerName(move.getPlayer().getUsername());
            simpleMove.setPlayerSymbol(getPlayerSymbol(move.getPlayer(), gameMatch));
            simpleMove.setXPosition(move.getXPosition());
            simpleMove.setYPosition(move.getYPosition());
            simpleMove.setMoveNumber(move.getMoveNumber());
            simpleMove.setMoveTime(move.getCreatedAt());

            simpleMoves.add(simpleMove);
        }

        return simpleMoves;
    }

    /**
     * Determines game result based on history and match
     */
    private String determineGameResult(GameHistory gameHistory, GameMatch gameMatch) {
        if (gameHistory.getWinnerId() == null) {
            return "DRAW";
        }
        
        if (gameHistory.getWinnerId().equals(gameMatch.getPlayerX().getId())) {
            return "X_WIN";
        } else if (gameHistory.getWinnerId().equals(gameMatch.getPlayerO().getId())) {
            return "O_WIN";
        }
        
        return "ONGOING";
    }

    /**
     * Reconstructs the final board state from moves
     */
    private int[][] reconstructBoardFromMoves(List<Move> moves, GameMatch gameMatch) {
        int[][] board = new int[15][15];

        for (Move move : moves) {
            int playerValue = getPlayerValue(move.getPlayer(), gameMatch);
            board[move.getXPosition()][move.getYPosition()] = playerValue;
        }

        return board;
    }

    /**
     * Builds player information for replay
     */
    private GameReplayResponse.PlayerInfo buildPlayerInfo(User player, String symbol, List<Move> allMoves) {
        GameReplayResponse.PlayerInfo info = new GameReplayResponse.PlayerInfo();
        info.setPlayerId(player.getId());
        info.setPlayerName(player.getUsername());
        info.setDisplayName(player.getDisplayName());
        info.setAvatarUrl(player.getAvatarUrl());
        info.setSymbol(symbol);

        // Count moves by this player
        List<Move> playerMoves = allMoves.stream()
                .filter(move -> move.getPlayer().getId().equals(player.getId()))
                .collect(Collectors.toList());

        info.setMoveCount(playerMoves.size());

        // Calculate average move time (simplified - use default value)
        info.setAverageMoveTime(25.0); // Default average move time in seconds

        return info;
    }

    /**
     * Builds game history summary for list view (optimized with minimal data)
     */
    private GameHistorySummaryResponse buildGameHistorySummary(GameHistory history, Long userId) {
        GameHistorySummaryResponse summary = new GameHistorySummaryResponse();
        summary.setGameId(history.getId());
        summary.setRoomId(history.getRoomId());
        summary.setWinnerId(history.getWinnerId());
        summary.setEndReason(history.getEndReason());
        summary.setGameStartTime(history.getGameStartedAt());
        summary.setGameEndTime(history.getGameEndedAt());
        summary.setIsUserWinner(userId.equals(history.getWinnerId()));

        // Calculate duration
        Long duration = calculateGameDuration(history.getGameStartedAt(), history.getGameEndedAt());
        summary.setGameDurationMinutes(duration);

        // Determine game result for current user
        String gameResult = determineUserGameResult(history, userId);
        summary.setGameResult(gameResult);

        // Get room name if available
        try {
            GameRoom room = gameRoomRepository.findById(history.getRoomId()).orElse(null);
            if (room != null) {
                summary.setRoomName(room.getName());
            }
        } catch (Exception e) {
            log.warn("Could not get room name for history {}: {}", history.getId(), e.getMessage());
        }

        // Get opponent info
        try {
            Long opponentId = userId.equals(history.getWinnerId()) ? history.getLoserId() : history.getWinnerId();
            if (opponentId != null) {
                User opponent = userRepository.findById(opponentId).orElse(null);
                if (opponent != null) {
                    summary.setOpponentName(opponent.getDisplayName() != null ? opponent.getDisplayName() : opponent.getUsername());
                    summary.setOpponentAvatar(opponent.getAvatarUrl());
                }
            }
        } catch (Exception e) {
            log.warn("Could not get opponent info for history {}: {}", history.getId(), e.getMessage());
        }

        return summary;
    }

    /**
     * Determines game result from user perspective
     */
    private String determineUserGameResult(GameHistory history, Long userId) {
        if (history.getWinnerId() == null) {
            return "DRAW";
        }
        return userId.equals(history.getWinnerId()) ? "WIN" : "LOSE";
    }

    /**
     * Gets player symbol (X or O) for a player in a match
     */
    private String getPlayerSymbol(User player, GameMatch gameMatch) {
        return player.getId().equals(gameMatch.getPlayerX().getId()) ? "X" : "O";
    }

    /**
     * Gets player value (1 for X, 2 for O) for a player in a match
     */
    private int getPlayerValue(User player, GameMatch gameMatch) {
        return player.getId().equals(gameMatch.getPlayerX().getId()) ? 1 : 2;
    }

    /**
     * Calculates game duration in minutes
     */
    private Long calculateGameDuration(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return 0L;
        }
        return Duration.between(startTime, endTime).toMinutes();
    }

    // Helper classes for internal use

    /**
     * Internal class for game time statistics
     */
    private static class GameTimeStatistics {
        final Long totalTimeMinutes;
        final Double averageDurationMinutes;
        final Long longestDurationMinutes;
        final Long shortestDurationMinutes;

        GameTimeStatistics(Long totalTime, Double averageTime, Long longestTime, Long shortestTime) {
            this.totalTimeMinutes = totalTime;
            this.averageDurationMinutes = averageTime;
            this.longestDurationMinutes = longestTime;
            this.shortestDurationMinutes = shortestTime;
        }
    }

    /**
     * Internal class for streak statistics
     */
    private static class StreakStatistics {
        final Integer currentWinStreak;
        final Integer bestWinStreak;

        StreakStatistics(Integer currentStreak, Integer bestStreak) {
            this.currentWinStreak = currentStreak;
            this.bestWinStreak = bestStreak;
        }
    }
}
