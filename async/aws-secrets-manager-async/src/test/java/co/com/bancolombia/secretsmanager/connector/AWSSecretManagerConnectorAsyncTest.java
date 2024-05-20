package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.config.AWSSecretsManagerConfig;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.InvalidParameterException;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AWSSecretManagerConnectorAsyncTest {
    @Mock
    private SecretsManagerAsyncClient client;
    @Mock
    private SecretsManagerAsyncClientBuilder clientBuilder;
    private AWSSecretManagerConnectorAsync connector;

    @BeforeEach
    void setUp() {
        AWSSecretsManagerConfig config = AWSSecretsManagerConfig.builder()
                .cacheSeconds(5)
                .cacheSize(10)
                .region(Region.US_EAST_1)
                .endpoint("http://localhost.com")
                .build();

        doReturn(client).when(clientBuilder).build();
        connector = new AWSSecretManagerConnectorAsync(config, clientBuilder);
    }

    @Test
    void shouldReturnSecretModel() {
        when(client.getSecretValue(getSecretValueRequest("secretModelName")))
                .thenReturn(getResponse("{\"username\":\"root\",\"password\":\"123456789\"}", true));

        StepVerifier.create(connector.getSecret("secretModelName", SecretModelTest.class))
                .expectNextMatches(secretModelTest -> secretModelTest.getUsername().equals("root")
                        && secretModelTest.getPassword().equals("123456789"))
                .expectComplete()
                .verify();
    }

    @Test
    void shouldReturnStringSecretValue() {
        when(client.getSecretValue(getSecretValueRequest("stringSecretName")))
                .thenReturn(getResponse("secretValue", true));

        StepVerifier.create(connector.getSecret("stringSecretName"))
                .expectNext("secretValue")
                .expectComplete()
                .verify();
    }

    @Test
    void shouldThrowExceptionWhenSecretIsNotAString() {
        when(client.getSecretValue(getSecretValueRequest("secretName")))
                .thenReturn(getResponse(null, true));

        StepVerifier.create(connector.getSecret("secretName"))
                .expectSubscription()
                .verifyError(SecretException.class);
    }

    @Test
    void shouldThrowExceptionWhenSecretIsNull() {
        when(client.getSecretValue(getSecretValueRequest("secretName")))
                .thenReturn(getResponse(null, false));

        StepVerifier.create(connector.getSecret("secretName"))
                .expectSubscription()
                .verifyError(SecretException.class);
    }

    @Test
    void shouldThrowExceptionWhenSecretIsNonExistent() {
        when(client.getSecretValue(getSecretValueRequest("secretName")))
                .thenReturn(CompletableFuture.failedFuture(ResourceNotFoundException.builder()
                        .message("Secrets Manager can't find the specified secret not found.")
                        .build()));

        StepVerifier.create(connector.getSecret("secretName"))
                .expectErrorMatches(err ->
                        err instanceof SecretException
                                && err.getMessage().equals("Secrets Manager can't find the specified secret not found."))
                .verify();
    }

    @Test
    void shouldThrowExceptionWhenRequestIsInvalid() {
        when(client.getSecretValue(getSecretValueRequest("secretNameF$1l")))
                .thenReturn(CompletableFuture.failedFuture(InvalidParameterException.builder()
                        .message("The parameter name or value is invalid.")
                        .build()));

        StepVerifier.create(connector.getSecret("secretNameF$1l"))
                .expectErrorMatches(err ->
                        err instanceof SecretException
                                && err.getMessage().equals("The parameter name or value is invalid."))
                .verify();
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
