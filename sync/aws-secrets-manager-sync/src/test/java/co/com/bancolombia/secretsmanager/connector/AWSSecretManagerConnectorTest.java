package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.model.AWSSecretDBModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Represents an AWS Connector Test. It lets you test AWS Secrets Manager Connector Object.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez P.</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class AWSSecretManagerConnectorTest {
    @Mock
    private SecretsManagerClientBuilder builder;
    @Mock
    private SecretsManagerClient client;
    private AWSSecretManagerConnector connector;

    @Test
    public void shouldConversionOk() throws SecretException {

        prepareClient("{\"username\":\"root\",\"password\":\"123456789\","
                + "\"engine\":\"oracle\",\""
                + "host\":\"jdbc:oracle:thin:@oauth-oracle.cufapur4ayuj"
                + ".us-east-1.rds.amazonaws.com:1521:ORCL\","
                + "\"port\":\"3306\",\"dbname\":\"ROOT\"}", true);

        connector = new AWSSecretManagerConnector("us-east-1", builder);

        AWSSecretDBModel model = connector.getSecret("SecretDBMock", AWSSecretDBModel.class);
        assertEquals("jdbc:oracle:thin:@oauth-oracle.cufapur4ayuj" +
                ".us-east-1.rds.amazonaws.com:1521:ORCL", model.getHost());
        assertEquals("oracle", model.getEngine());
        assertEquals("root", model.getUsername());
        assertEquals("123456789", model.getPassword());
        assertEquals("3306", model.getPort());
        assertEquals("ROOT", model.getDbname());
    }

    @Test(expected = SecretException.class)
    public void shouldConversionFail() throws Exception {
        prepareClient("test", true);
        connector = new AWSSecretManagerConnector("us-east-1", builder);
        connector.getSecret("SecretDBFailMock", AWSSecretDBModel.class);
    }

    @Test
    public void shouldGetStringSecret() throws SecretException {
        prepareClient("SecretValue", true);
        connector = new AWSSecretManagerConnector("us-east-1", builder);

        String secretValue = connector.getSecret("secretName");
        assertEquals("SecretValue", secretValue);
    }

    @Test(expected = SecretException.class)
    public void shouldThrowExceptionWhenSecretIsNotAString() throws SecretException {
        prepareClient(null, true);
        connector = new AWSSecretManagerConnector("us-east-1", builder);
        connector.getSecret("secretName");
    }

    @Test(expected = SecretException.class)
    public void shouldThrowExceptionWhenSecretResultIsNull() throws SecretException {
        prepareClient(null, false);
        connector = new AWSSecretManagerConnector("us-east-1", builder);
        connector.getSecret("secretName");
    }

    @Test(expected = SecretException.class)
    public void shouldThrowExceptionWhenSecretIsNonExistent() throws SecretException {
        prepareClient(null, false);
        connector = new AWSSecretManagerConnector("us-east-1", builder);

        when(client.getSecretValue(any(GetSecretValueRequest.class))).thenThrow(ResourceNotFoundException.class);

        connector.getSecret("secretName");
    }

    @Test(expected = SecretException.class)
    public void shouldThrowExceptionWhenSecretManagerFailsWithEndpoint() throws SecretException {
        prepareClient(null, false);
        connector = new AWSSecretManagerConnector("us-east-1", builder);

        when(client.getSecretValue(any(GetSecretValueRequest.class))).thenThrow(SdkClientException.class);

        connector.getSecret("secretName");
    }

    private void prepareClient(String data, boolean secretValue) {
        GetSecretValueResponse responseMock = secretValue ? GetSecretValueResponse.builder()
                .secretString(data)
                .build() : null;

        when(builder.build()).thenReturn(client);

        when(client.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(responseMock);
    }

}
