package co.com.bancolombia.commons.secretsmanager.connector.clients;

import co.com.bancolombia.commons.secretsmanager.connector.models.AWSSecretDBModel;
import co.com.bancolombia.commons.secretsmanager.exceptions.SecretException;
import co.com.bancolombia.commons.secretsmanager.manager.GenericManager;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Represents an AWS Connector Test. It lets you to test AWS Secrets Manager Connector Object.
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez P.</a>
 */
public class AWSSecretManagerConnectorTest {
  @Test
  public void conversionOk() throws SecretException {
    AWSSecretManagerConnector mockConnector = mock(AWSSecretManagerConnector.class);
    when(mockConnector.getSecret("SecretDBMock"))
        .thenReturn("{\"username\":\"root\",\"password\":\"123456789\","
            + "\"engine\":\"oracle\",\""
            + "host\":\"jdbc:oracle:thin:@oauth-oracle.cufapur4ayuj"
            + ".us-east-1.rds.amazonaws.com:1521:ORCL\","
            + "\"port\":\"3306\",\"dbname\":\"ROOT\"}");
    GenericManager manager = new GenericManager(mockConnector);
    AWSSecretDBModel model = manager.getSecretModel("SecretDBMock", AWSSecretDBModel.class);
    assertNotNull(model);
  }
  
  @Test(expected = SecretException.class)
  public void conversionFail() throws Exception {
    AWSSecretManagerConnector mockConnector = mock(AWSSecretManagerConnector.class);
    when(mockConnector.getSecret("SecretDBFailMock")).thenReturn("test");
    GenericManager manager = new GenericManager(mockConnector);
    AWSSecretDBModel model = manager.getSecretModel("SecretDBFailMock", AWSSecretDBModel.class);
  }
}
