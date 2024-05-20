package co.com.bancolombia.secretsmanager.connector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Represents an Environment Connector Test. It lets you to test Environment Connector Object.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez P.</a>
 */
class EnvConnectorTest {

    private EnvConnector connector;

    @BeforeEach
    void setUp() {
        connector = new EnvConnector();
    }

    @Test
    void variableDoesNotExist() throws Exception {
        assertThrows(Exception.class, () -> {
            connector.getSecret("SecretDoesNotExist");
        });
    }

    @Test
    void variableExists() throws Exception {
        String secret = connector.getSecret("PATH");
        assertNotNull(secret);
    }

    @Test
    void getSecretModel() {
        assertThrows(UnsupportedOperationException.class, () -> connector.getSecret("mySecret", Class.class));
    }
}