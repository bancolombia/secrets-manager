package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.SsmClientBuilder;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SsmClient.class)
public class AWSParameterStoreConnectorTest {

    private AWSParameterStoreConnector connector;
    private SsmClient clientMock;

    @Test
    public void shouldGetStringSecretWithEndpoint() throws SecretException {
        connector = new AWSParameterStoreConnector("us-east-1","endpoint");
        prepareClient("secretValue",true);
        String secretValue = connector.getSecret("secretName");
        assertEquals(secretValue,"secretValue");
    }

    @Test
    public void shouldGetStringSecret() throws SecretException {
        connector = new AWSParameterStoreConnector("us-east-1");
        prepareClient("secretValue",true);
        String secretValue = connector.getSecret("secretName");
        assertEquals(secretValue,"secretValue");
    }

    @Test(expected = SecretException.class)
    public void shouldThrowExceptionWhenSecretValueNull() throws SecretException {
        prepareClient("secretValue",false);
        connector = new AWSParameterStoreConnector("us-east-1");
        connector.getSecret("secretName");
    }

    @Test(expected = SecretException.class)
    public void shouldThrowExceptionWhenSecretIsNotAString() throws SecretException {
        prepareClient(null,true);
        connector = new AWSParameterStoreConnector("us-east-1");
        connector.getSecret("secretName");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowExceptionWhenNoApplySerialization() throws UnsupportedOperationException {
        connector = new AWSParameterStoreConnector("us-east-1");
        connector.getSecret("secretName", Class.class);
    }

    @Test(expected = SecretException.class)
    public void shouldThrowExceptionWhenParameterNotFound() throws SecretException {
        prepareClient(null, false);
        connector = new AWSParameterStoreConnector("us-east-1");

        when(clientMock.getParameter(any(GetParameterRequest.class))).thenThrow(ParameterNotFoundException.class);

        connector.getSecret("secretName");
    }

    private void prepareClient(String data,boolean secretValue){
        SsmClientBuilder clientBuilderMock = Mockito.mock(SsmClientBuilder.class);
        clientMock = Mockito.mock(SsmClient.class);
        GetParameterResponse responseMock = secretValue ? GetParameterResponse.builder()
                .parameter(Parameter.builder().value(data).build())
                .build() : null;

        PowerMockito.mockStatic(SsmClient.class);
        when(SsmClient.builder()).thenReturn(clientBuilderMock);
        when(clientBuilderMock.credentialsProvider(any())).thenReturn(clientBuilderMock);
        when(clientBuilderMock.region(any())).thenReturn(clientBuilderMock);
        when(clientBuilderMock.build()).thenReturn(clientMock);

        when(clientMock.getParameter(any(GetParameterRequest.class))).thenReturn(responseMock);
    }

}
