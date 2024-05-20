package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Represents an File Connector Test. It lets you to test File Connector Object.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez P.</a>
 */
class FileConnectorTest {
    private FileConnector connector;

    @Test
    void pathDoesNotExist() throws Exception {
        connector = new FileConnector("/path/doesNot/exits");
        assertThrows(SecretException.class, () -> connector.getSecret("Secret.txt"));
    }

    @Test
    void pathExists() throws SecretException {
        connector = new FileConnector("src/test/resources/");
        String secret = connector.getSecret("Secret.txt");
        assertEquals("secret", secret);
    }

    @Test
    void getSecretModel() {
        connector = new FileConnector("src/test/resources/");
        assertThrows(UnsupportedOperationException.class, () -> connector.getSecret("Secret.txt", Class.class));
    }

}