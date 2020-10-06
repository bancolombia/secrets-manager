import api.GenericManager;
import api.GenericManagerAsync;
import exceptions.SecretException;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClientBuilder;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

public class AWSSecretManagerConnector implements GenericManagerAsync {

    private Region region;
    private Optional<URI> endpoint;

    public AWSSecretManagerConnector(String region) {
        setRegion(region);
        endpoint = Optional.empty();
    }

    /**
     * This constructor allows make a connection for a local instance of
     * AWS Secrets Manager, such as: LocalStack, Docker container, etc.
     *
     * @param endpoint : String uri connection
     * @param region   : Dummy region for Amazon SDK Client
     */
    public AWSSecretManagerConnector(String region, String endpoint) {
        this.endpoint = Optional.of(URI.create(endpoint));
        this.region = Region.of(region);
    }

    private void setRegion(String region) {
        this.region = Region.of(region);
    }

    @Override
    public Mono<String> getSecret(String secretName) throws SecretException {
        SecretsManagerAsyncClient client = buildClient();
        return getSecret(secretName, client);
    }

    @Override
    public <T> Mono<T> getSecret(String secretName, Class<T> cls) throws SecretException {
        return this.getSecret(secretName)
                .flatMap((data->Mono.just(GsonUtils.getInstance().stringToModel(data, cls))))
                .onErrorMap((e)->new SecretException(e.getMessage()));
    }

    private Mono<String> getSecret(String secretName, SecretsManagerAsyncClient client) throws SecretException {
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();
        GetSecretValueResponse getSecretValueResult = null;


        return Mono.fromFuture(client.getSecretValue(getSecretValueRequest))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new SecretException("Secret value is null"))))
                .flatMap(secretResult -> {
                    if (secretResult.secretString() != null) {
                        return Mono.just(secretResult.secretString());
                    }
                    return Mono.error(new SecretException("Secret value is not a String"));
                }).cache(Duration.ofSeconds(5));
    }

    /**
     * Default provider chain extended with extra CredentialProvider and
     * specif order defined.
     *
     * @see AwsCredentialsProviderChain
     */
    private AwsCredentialsProviderChain getProviderChain() {
        return AwsCredentialsProviderChain.builder()
                .addCredentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .addCredentialsProvider(SystemPropertyCredentialsProvider.create())
                .addCredentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
                .addCredentialsProvider(ProfileCredentialsProvider.create())
                .addCredentialsProvider(ContainerCredentialsProvider.builder().build())
                .addCredentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();
    }

    private SecretsManagerAsyncClient buildClient() {
        SecretsManagerAsyncClientBuilder clientBuilder = SecretsManagerAsyncClient.builder()
                .credentialsProvider(getProviderChain())
                .region(region);

        endpoint.ifPresent(clientBuilder::endpointOverride);
        return clientBuilder.build();
    }

}
