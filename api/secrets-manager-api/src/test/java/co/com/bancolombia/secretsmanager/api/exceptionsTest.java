package co.com.bancolombia.secretsmanager.api;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class exceptionsTest {

    @Test
    public void generateExpetion() {
        SecretException ex = new SecretException("My error");
        assertEquals("My error", ex.getMessage());
    }
}
