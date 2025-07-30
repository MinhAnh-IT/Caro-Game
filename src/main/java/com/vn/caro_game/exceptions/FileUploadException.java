package com.vn.caro_game.exceptions;

/**
 * Exception thrown when file upload operations fail.
 *
 * <p>This exception is used to indicate errors during file upload processes,
 * including validation failures, I/O errors, and storage issues.</p>
 *
 * @author Caro Game Team
 * @since 1.0.0
 */
public class FileUploadException extends RuntimeException {

    /**
     * Constructs a new FileUploadException with the specified detail message.
     *
     * @param message the detail message
     */
    public FileUploadException(String message) {
        super(message);
    }

    /**
     * Constructs a new FileUploadException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public FileUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
