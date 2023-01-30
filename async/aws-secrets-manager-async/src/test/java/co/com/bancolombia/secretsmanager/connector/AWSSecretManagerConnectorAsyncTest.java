package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.config.AWSSecretsManagerConfig;
import lombok.Data;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.test.StepVerifier;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SecretsManagerAsyncClient.class)
public class AWSSecretManagerConnectorAsyncTest {

    private static AWSSecretsManagerConfig config;
    private AWSSecretManagerConnectorAsync connector;
    private SecretsManagerAsyncClient clientMock;

    @BeforeClass
    public static void init() {
        config = AWSSecretsManagerConfig.builder()
                .cacheSeconds(5)
                .cacheSize(10)
                .region(Region.US_EAST_1)
                .endpoint("http://localhost.com")
                .build();
    }

    @Before
    public void setUp() {
        prepareClient();
    }

    @Test
    public void shouldReturnSecretModel() {
        when(clientMock.getSecretValue(getSecretValueRequest("secretModelName")))
                .thenReturn(getResponse("{\"username\":\"root\",\"password\":\"123456789\"}", true));

        StepVerifier.create(connector.getSecret("secretModelName", SecretModelTest.class))
                .expectNextMatches(secretModelTest -> secretModelTest.getUsername().equals("root")
                        && secretModelTest.getPassword().equals("123456789"))
                .expectComplete()
                .verify();
    }

    @Test
    public void shouldReturnStringSecretValue() {
        when(clientMock.getSecretValue(getSecretValueRequest("stringSecretName")))
                .thenReturn(getResponse("secretValue", true));

        StepVerifier.create(connector.getSecret("stringSecretName"))
                .expectNext("secretValue")
                .expectComplete()
                .verify();
    }

    @Test
    public void shouldThrowExceptionWhenSecretIsNotAString() {
        when(clientMock.getSecretValue(getSecretValueRequest("secretName")))
                .thenReturn(getResponse(null, true));

        StepVerifier.create(connector.getSecret("secretName"))
                .expectSubscription()
                .verifyError(SecretException.class);
    }

    @Test
    public void shouldThrowExceptionWhenSecretIsNull() {
        when(clientMock.getSecretValue(getSecretValueRequest("secretName")))
                .thenReturn(getResponse(null, false));

        StepVerifier.create(connector.getSecret("secretName"))
                .expectSubscription()
                .verifyError(SecretException.class);
    }

    @Test
    public void shouldThrowExceptionWhenSecretIsNonExistent() {
        ResourceNotFoundException a = ResourceNotFoundException.builder()
                .message("Secrets Manager can't find the specified secret not found.")
                .build();

        when(clientMock.getSecretValue(getSecretValueRequest("secretName")))
                .thenThrow(a);

        StepVerifier.create(connector.getSecret("secretName"))
                .expectErrorMatches(err ->
                        err instanceof SecretException
                        && err.getMessage().equals("Secrets Manager can't find the specified secret not found."))
                .verify();
    }

    private void prepareClient() {
        SecretsManagerAsyncClientBuilder clientBuilderMock = Mockito.mock(SecretsManagerAsyncClientBuilder.class);
        clientMock = Mockito.mock(SecretsManagerAsyncClient.class);

        PowerMockito.mockStatic(SecretsManagerAsyncClient.class);
        when(SecretsManagerAsyncClient.builder()).thenReturn(clientBuilderMock);
        when(clientBuilderMock.credentialsProvider(any())).thenReturn(clientBuilderMock);
        when(clientBuilderMock.region(any())).thenReturn(clientBuilderMock);
        when(clientBuilderMock.build()).thenReturn(clientMock);

        connector = new AWSSecretManagerConnectorAsync(config);
    }

    private GetSecretValueRequest getSecretValueRequest(String secretName) {
        return GetSecretValueRequest.builder().secretId(secretName).build();
    }

    private CompletableFuture<GetSecretValueResponse> getResponse(String data, boolean secretValue) {
        GetSecretValueResponse responseMock = secretValue ? GetSecretValueResponse.builder()
                .secretString(data)
                .build() : null;

        CompletableFuture<GetSecretValueResponse> secretResponse = new CompletableFuture<>();
        secretResponse.complete(responseMock);

        return secretResponse;
    }

    @Data
    private static class SecretModelTest {

        private final String username;
        private final String password;

    }

}
