package de.rhm176.modmenu.api.update;

import org.jetbrains.annotations.ApiStatus;

/**
 * An unchecked exception that should be thrown when an error occurs during the update checking process.
 * <p>
 * This exception is intended to wrap any issues that might arise while communicating
 * with a remote server, parsing response data, or comparing versions, providing a
 * clear signal that the update check has failed. Mod Menu will gracefully handle it.
 *
 * @see UpdateChecker
 * @since 1.0.0
 */
@ApiStatus.AvailableSince("1.0.0")
public class UpdateCheckException extends RuntimeException {
    /**
     * Constructs a new {@code UpdateCheckException} with the specified detail message.
     *
     * @param message The detail message, which is saved for later retrieval
     * by the {@link #getMessage()} method.
     */
    public UpdateCheckException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code UpdateCheckException} with the specified detail message
     * and cause.
     *
     * @param message The detail message, which is saved for later retrieval
     * by the {@link #getMessage()} method.
     * @param cause   The cause (e.g., an {@link java.io.IOException} or
     * {@link InterruptedException}), which is saved for later
     * retrieval by the {@link #getCause()} method. A {@code null}
     * value is permitted and indicates that the cause is nonexistent
     * or unknown.
     */
    public UpdateCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}
