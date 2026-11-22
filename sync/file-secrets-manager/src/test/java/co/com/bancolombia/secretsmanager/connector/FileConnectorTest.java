package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import org.junit.jupiter.api.Test;

import java.nio.file.NoSuchFileException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Represents an File Connector Test. It lets you to test File Connector Object.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez P.</a>
 */
class FileConnectorTest {
    private FileConnector connector;

    @Test
    void pathDoesNotExist() {
        connector = new FileConnector("/path/doesNot/exits");
        SecretException ex = assertThrows(SecretException.class, () -> connector.getSecret("Secret.txt"));
        assertInstanceOf(NoSuchFileException.class, ex.getCause());
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