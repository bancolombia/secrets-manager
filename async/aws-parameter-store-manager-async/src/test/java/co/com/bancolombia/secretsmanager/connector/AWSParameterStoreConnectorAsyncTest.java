package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.config.AWSParameterStoreConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.test.StepVerifier;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.SsmAsyncClientBuilder;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AWSParameterStoreConnectorAsyncTest {
    @Mock
    private SsmAsyncClient client;
    @Mock
    private SsmAsyncClientBuilder builder;
    private AWSParameterStoreConnectorAsync connector;
    private AWSParameterStoreConfig config;

    @Before
    public void buildClient() {
        config = AWSParameterStoreConfig.builder()
                .cacheSeconds(1)
                .cacheSize(10)
                .region(Region.US_EAST_1)
                .endpoint("http://localhost.com")
                .build();
    }

    @Test
    public void shouldGetStringSecret() throws SecretException {
        prepareClient("secretValue", true);
        StepVerifier.create(connector.getSecret("secretName"))
                .expectNext("secretValue").expectComplete().verify();
    }

    @Test
    public void shouldThrowExceptionWhenSecretValueNull() {
        prepareClient("secretValue", false);
        StepVerifier.create(connector.getSecret("secretName"))
                .expectSubscription()
                .verifyError(SecretException.class);
    }

    @Test
    public void shouldThrowExceptionWhenSecretIsNotAString() {
        prepareClient(null, true);
        StepVerifier.create(connector.getSecret("secretName"))
                .expectSubscription()
                .verifyError(SecretException.class);
    }

    @Test
    public void shouldThrowExceptionWhenNoApplySerialization() {
        prepareClient("secretValue", true);
        StepVerifier.create(connector.getSecret("secretName", Class.class))
                .expectSubscription()
                .verifyError(UnsupportedOperationException.class);
    }

    private void prepareClient(String data, boolean secretValue) {
        GetParameterResponse responseMock = secretValue ? GetParameterResponse.builder()
                .parameter(Parameter.builder().value(data).build())
                .build() : null;
        CompletableFuture<GetParameterResponse> completableFuture = new CompletableFuture<>();
        completableFuture.complete(responseMock);

        when(builder.credentialsProvider(any())).thenReturn(builder);
        when(builder.region(any())).thenReturn(builder);
        when(builder.build()).thenReturn(client);

        when(client.getParameter(any(GetParameterRequest.class))).thenReturn(completableFuture);
        connector = new AWSParameterStoreConnectorAsync(config, builder);
    }

}
