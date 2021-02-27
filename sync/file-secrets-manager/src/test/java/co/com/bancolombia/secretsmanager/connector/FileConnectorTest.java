package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Represents an File Connector Test. It lets you to test File Connector Object.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez P.</a>
 */
public class FileConnectorTest {
    private FileConnector connector;

    @Test(expected = SecretException.class)
    public void pathDoesntExist() throws Exception {
        connector = new FileConnector("/path/doesnt/exits");
        connector.getSecret("SecretDoesntExist");
    }

    @Test
    public void pathExists() throws SecretException {
        connector = new FileConnector("src/test/resources/");
        String secreto = connector.getSecret("Secret.txt");
        assertEquals("secret", secreto);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getSecretModel() {
        connector = new FileConnector("src/test/resources/");
        connector.getSecret("Secret.txt", Class.class);
    }

}