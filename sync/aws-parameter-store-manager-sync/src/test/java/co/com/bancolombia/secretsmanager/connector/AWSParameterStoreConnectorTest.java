package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.SsmClientBuilder;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AWSParameterStoreConnectorTest {
    @Mock
    private SsmClient client;
    @Mock
    private SsmClientBuilder builder;

    private AWSParameterStoreConnector connector;

    @Test
    public void shouldGetStringSecretWithEndpoint() throws SecretException {
        prepareClient("secretValue", true);
        connector = new AWSParameterStoreConnector("us-east-1", builder);
        String secretValue = connector.getSecret("secretName");
        assertEquals(secretValue, "secretValue");
    }

    @Test
    public void shouldGetStringSecret() throws SecretException {
        prepareClient("secretValue", true);
        connector = new AWSParameterStoreConnector("us-east-1", builder);
        String secretValue = connector.getSecret("secretName");
        assertEquals(secretValue, "secretValue");
    }

    @Test(expected = SecretException.class)
    public void shouldThrowExceptionWhenSecretValueNull() throws SecretException {
        prepareClient("secretValue", false);
        connector = new AWSParameterStoreConnector("us-east-1", builder);
        connector.getSecret("secretName");
    }

    @Test(expected = SecretException.class)
    public void shouldThrowExceptionWhenSecretIsNotAString() throws SecretException {
        prepareClient(null, true);
        connector = new AWSParameterStoreConnector("us-east-1", builder);
        connector.getSecret("secretName");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowExceptionWhenNoApplySerialization() throws UnsupportedOperationException {
        prepareClient("secretValue", true);
        connector = new AWSParameterStoreConnector("us-east-1", builder);
        connector.getSecret("secretName", Class.class);
    }

    @Test(expected = SecretException.class)
    public void shouldThrowExceptionWhenParameterNotFound() throws SecretException {
        prepareClient(null, false);
        connector = new AWSParameterStoreConnector("us-east-1", builder);

        when(client.getParameter(any(GetParameterRequest.class))).thenThrow(ParameterNotFoundException.class);

        connector.getSecret("secretName");
    }

    private void prepareClient(String data, boolean secretValue) {
        GetParameterResponse responseMock = secretValue ? GetParameterResponse.builder()
                .parameter(Parameter.builder().value(data).build())
                .build() : null;

        when(builder.credentialsProvider(any())).thenReturn(builder);
        when(builder.region(any())).thenReturn(builder);
        when(builder.build()).thenReturn(client);

        when(client.getParameter(any(GetParameterRequest.class))).thenReturn(responseMock);
    }

}
