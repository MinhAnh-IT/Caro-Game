package com.vn.caro_game.enums;

/**
 * Enum representing the rematch request state
 */
public enum RematchState {
    /**
     * No rematch request in progress
     */
    NONE,
    
    /**
     * Someone requested a rematch
     */
    REQUESTED,
    
    /**
     * Both players accepted the rematch
     */
    BOTH_ACCEPTED,
    
    /**
     * New rematch room has been created
     */
    CREATED
}
