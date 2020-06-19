package co.com.bancolombia.commons.secretsmanager.manager;

import co.com.bancolombia.commons.secretsmanager.connector.clients.EnvConnector;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/** Represents an Generic Manager Test. It lets you to test a Generic Manager Object.
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez P.</a>
 */
public class GenericManagerTest {
  private EnvConnector connector;
  
  @Before
  public void setUp() {
    connector = new EnvConnector();
  }
  
  @Test(expected = Exception.class) 
  public void variableDoesntExist() throws Exception {
    AbstractManager manager = new GenericManager(connector);
    manager.getSecret("SecretDoesntExist");
  }
  
  @Test
  public void variableExists() throws Exception {
    AbstractManager manager = new GenericManager(connector);
    String secret = manager.getSecret("PATH");
    assertNotNull(secret);
    assertFalse("".equals(secret));
  }
}
