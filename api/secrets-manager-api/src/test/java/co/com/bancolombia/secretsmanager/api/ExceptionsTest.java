package co.com.bancolombia.secretsmanager.api;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ExceptionsTest {

    @Test
    void generateException() {
        SecretException ex = new SecretException("My error");
        assertEquals("My error", ex.getMessage());
    }

    @Test
    void generateExceptionWithCause() {
        IllegalStateException cause = new IllegalStateException("root");
        SecretException ex = new SecretException("My error", cause);
        assertEquals("My error", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void generateExceptionWithCauseWithoutMessage() {
        IllegalStateException cause = new IllegalStateException();
        SecretException ex = new SecretException(cause.getMessage(), cause);
        assertEquals("java.lang.IllegalStateException", ex.getMessage());
        assertSame(cause, ex.getCause());
    }
}
