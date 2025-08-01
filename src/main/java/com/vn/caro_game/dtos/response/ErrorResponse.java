package com.vn.caro_game.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard error response format for API documentation.
 * Used in Swagger documentation to show proper error response structure.
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Error response format")
public class ErrorResponse {
    
    @Schema(description = "Indicates if the request was successful", example = "false")
    private boolean success;
    
    @Schema(description = "HTTP status code", example = "500")
    private int statusCode;
    
    @Schema(description = "Error message", example = "Internal server error occurred")
    private String message;
    
    @Schema(description = "Always null for error responses", example = "null")
    private Object data;
    
    @Schema(description = "Internal error code for debugging", example = "INTERNAL_SERVER_ERROR")
    private String errorCode;
}
