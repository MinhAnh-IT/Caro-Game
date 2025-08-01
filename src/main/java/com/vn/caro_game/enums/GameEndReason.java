package com.vn.caro_game.enums;

/**
 * Enum representing how a game ended
 */
public enum GameEndReason {
    /**
     * Normal game completion (someone won)
     */
    WIN,
    
    /**
     * Game ended by surrender
     */
    SURRENDER,
    
    /**
     * Game ended because someone left
     */
    LEAVE,
    
    /**
     * Game ended by timeout
     */
    TIMEOUT,
    
    /**
     * Game ended by system/admin
     */
    SYSTEM
}
