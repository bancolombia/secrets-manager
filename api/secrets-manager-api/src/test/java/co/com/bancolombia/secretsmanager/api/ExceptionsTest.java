package co.com.bancolombia.secretsmanager.api;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionsTest {

    @Test
    void generateException() {
        SecretException ex = new SecretException("My error");
        assertEquals("My error", ex.getMessage());
    }
}
