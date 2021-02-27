package co.com.bancolombia.secretsmanager.connector;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Represents an Environment Connector Test. It lets you to test Environment Connector Object.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez P.</a>
 */
public class EnvConnectorTest {

    private EnvConnector connector;

    @Before
    public void setUp() {
        connector = new EnvConnector();
    }

    @Test(expected = Exception.class)
    public void variableDoesntExist() throws Exception {
        connector.getSecret("SecretDoesntExist");
    }

    @Test
    public void variableExists() throws Exception {
        String secret = connector.getSecret("PATH");
        assertNotNull(secret);
    }
}