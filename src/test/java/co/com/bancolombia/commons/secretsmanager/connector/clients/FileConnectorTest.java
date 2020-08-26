package co.com.bancolombia.commons.secretsmanager.connector.clients;

import co.com.bancolombia.commons.secretsmanager.exceptions.SecretException;
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

}
