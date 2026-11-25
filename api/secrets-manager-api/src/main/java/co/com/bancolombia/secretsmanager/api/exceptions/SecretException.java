package co.com.bancolombia.secretsmanager.api.exceptions;

/**
 * Represents an Secret Exception.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez
 * P.</a>
 */
public class SecretException extends Exception {

    /**
     * Creates a SecretException with the given message.
     *
     * @param message the detail message
     */
    public SecretException(String message) {
        super(message);
    }

    /**
     * Creates a SecretException that keeps the original exception as its cause,
     * so its type and stack trace are preserved. If {@code message} is null, the
     * cause's {@code toString()} is used as the message.
     *
     * @param message the detail message
     * @param cause   the original exception
     */
    public SecretException(String message, Throwable cause) {
        super(message == null && cause != null ? cause.toString() : message, cause);
    }

}
