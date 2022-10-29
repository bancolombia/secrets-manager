package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.model.AWSSecretDBModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
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
@RunWith(PowerMockRunner.class)
@PrepareForTest({SecretsManagerClient.class})
public class AWSSecretManagerConnectorTest {

    private AWSSecretManagerConnector connector;
    private SecretsManagerClient clientMock;

    @Test
    public void shouldConversionOk() throws SecretException {

        prepareClient("{\"username\":\"root\",\"password\":\"123456789\","
                + "\"engine\":\"oracle\",\""
                + "host\":\"jdbc:oracle:thin:@oauth-oracle.cufapur4ayuj"
                + ".us-east-1.rds.amazonaws.com:1521:ORCL\","
                + "\"port\":\"3306\",\"dbname\":\"ROOT\"}", true);

        connector = new AWSSecretManagerConnector("us-east-1");

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
        connector = new AWSSecretManagerConnector("us-east-1");
        connector.getSecret("SecretDBFailMock", AWSSecretDBModel.class);
    }

    @Test
    public void shouldGetStringSecret() throws SecretException {
        prepareClient("SecretValue", true);
        connector = new AWSSecretManagerConnector("us-east-1");

        String secretValue = connector.getSecret("secretName");
        assertEquals("SecretValue", secretValue);
    }

    @Test(expected = SecretException.class)
    public void shouldThrowExceptionWhenSecretIsNotAString() throws SecretException {
        prepareClient(null, true);
        connector = new AWSSecretManagerConnector("us-east-1");
        connector.getSecret("secretName");
    }

    @Test(expected = SecretException.class)
    public void shouldThrowExceptionWhenSecretResultIsNull() throws SecretException {
        prepareClient(null, false);
        connector = new AWSSecretManagerConnector("us-east-1");
        connector.getSecret("secretName");
    }

    @Test(expected = SecretException.class)
    public void shouldThrowExceptionWhenSecretIsNonExistent() throws SecretException {
        prepareClient(null, false);
        connector = new AWSSecretManagerConnector("us-east-1");

        when(clientMock.getSecretValue(any(GetSecretValueRequest.class))).thenThrow(ResourceNotFoundException.class);

        connector.getSecret("secretName");
    }

    @Test(expected = SecretException.class)
    public void shouldThrowExceptionWhenSecretManagerFailsWithEndpoint() throws SecretException {
        prepareClient(null, false);
        connector = new AWSSecretManagerConnector("us-east-1", "http://localhost:4566");

        when(clientMock.getSecretValue(any(GetSecretValueRequest.class))).thenThrow(SdkClientException.class);

        connector.getSecret("secretName");
    }

    private void prepareClient(String data, boolean secretValue) {
        SecretsManagerClientBuilder clientBuilderMock = Mockito.mock(SecretsManagerClientBuilder.class);
        clientMock = Mockito.mock(SecretsManagerClient.class);
        GetSecretValueResponse responseMock = secretValue ? GetSecretValueResponse.builder()
                .secretString(data)
                .build() : null;

        PowerMockito.mockStatic(SecretsManagerClient.class);
        when(SecretsManagerClient.builder()).thenReturn(clientBuilderMock);
        when(clientBuilderMock.credentialsProvider(any())).thenReturn(clientBuilderMock);
        when(clientBuilderMock.region(any())).thenReturn(clientBuilderMock);
        when(clientBuilderMock.build()).thenReturn(clientMock);

        when(clientMock.getSecretValue(any(GetSecretValueRequest.class))).thenReturn(responseMock);
    }

}
