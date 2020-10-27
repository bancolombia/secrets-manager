package connector.clients;

import connector.AWSSecretManagerConnector;
import exceptions.SecretException;
import model.AWSSecretDBModel;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Represents an AWS Connector Test. It lets you to test AWS Secrets Manager Connector Object.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez P.</a>
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({SecretsManagerClient.class})
public class AWSSecretManagerConnectorTest {
    @Test
    @Ignore
    public void shouldConversionOk() throws SecretException {
        AWSSecretManagerConnector mockConnector = mock(AWSSecretManagerConnector.class);
        when(mockConnector.getSecret("SecretDBMock"))
                .thenReturn("{\"username\":\"root\",\"password\":\"123456789\","
                        + "\"engine\":\"oracle\",\""
                        + "host\":\"jdbc:oracle:thin:@oauth-oracle.cufapur4ayuj"
                        + ".us-east-1.rds.amazonaws.com:1521:ORCL\","
                        + "\"port\":\"3306\",\"dbname\":\"ROOT\"}");
        AWSSecretDBModel model = mockConnector.getSecret("SecretDBMock", AWSSecretDBModel.class);
        assertNotNull(model);
    }

    @Test(expected = SecretException.class)
    @Ignore
    public void shouldConversionFail() throws Exception {
        AWSSecretManagerConnector mockConnector = mock(AWSSecretManagerConnector.class);
        when(mockConnector.getSecret("SecretDBFailMock")).thenReturn("test");
        AWSSecretDBModel model = mockConnector.getSecret("SecretDBFailMock", AWSSecretDBModel.class);
    }

    @Test
    @Ignore
    public void shouldGetStringSecret() throws SecretException {
        AWSSecretManagerConnector secret = new AWSSecretManagerConnector("us-east-1");
        SecretsManagerClient client = mock(SecretsManagerClient.class);

        GetSecretValueResponse response = GetSecretValueResponse.builder().secretString("SecretValue").build();

        when(client.getSecretValue(Mockito.any(GetSecretValueRequest.class))).thenReturn(response);

        PowerMockito.mockStatic(SecretsManagerClient.class);
        SecretsManagerClientBuilder builder = mock(SecretsManagerClientBuilder.class);
        when(SecretsManagerClient.builder()).thenReturn(builder);
        when(builder.region(Mockito.any())).thenReturn(builder);
        when(builder.build()).thenReturn(client);

        String secretValue = secret.getSecret("secretName");
        assertEquals("SecretValue", secretValue);
    }

    @Test(expected = SecretException.class)
    @Ignore
    public void shouldThrowExceptionWhenSecretIsNotAString() throws SecretException {
        AWSSecretManagerConnector secret = new AWSSecretManagerConnector("us-east-1", "localhost:4566");
        SecretsManagerClient client = mock(SecretsManagerClient.class);

        GetSecretValueResponse response = GetSecretValueResponse.builder().secretString(null).build();

        when(client.getSecretValue(Mockito.any(GetSecretValueRequest.class))).thenReturn(response);

        PowerMockito.mockStatic(SecretsManagerClient.class);
        SecretsManagerClientBuilder builder = mock(SecretsManagerClientBuilder.class);
        when(SecretsManagerClient.builder()).thenReturn(builder);
        when(builder.region(Mockito.any())).thenReturn(builder);
        when(builder.build()).thenReturn(client);

        secret.getSecret("secretName");
    }
}
