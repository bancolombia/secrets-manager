package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.GenericManagerAsync;
import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.commons.utils.GsonUtils;
import co.com.bancolombia.secretsmanager.config.AWSSecretsManagerConfig;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.ContainerCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class AWSSecretManagerConnectorAsync implements GenericManagerAsync {

    private final AWSSecretsManagerConfig config;
    private final SecretsManagerAsyncClient client;
    private final AsyncCache<String, String> cache;
    private final Logger logger = Logger.getLogger("connector.AWSSecretManagerConnector");

    public AWSSecretManagerConnectorAsync(AWSSecretsManagerConfig config) {
        this.config = config;
        this.client = buildClient(SecretsManagerAsyncClient.builder());
        this.cache = initCache();
    }

    /**
     * For unit tests
     * @param config
     * @param builder
     */
    public AWSSecretManagerConnectorAsync(AWSSecretsManagerConfig config, SecretsManagerAsyncClientBuilder builder) {
        this.config = config;
        this.client = buildClient(builder);
        this.cache = initCache();
    }

    @Override
    public Mono<String> getSecret(String secretName) {
        return Mono.fromFuture(cache.get(secretName,
                (s, ex) -> getSecretValue(secretName).toFuture().toCompletableFuture()));
    }

    @Override
    public <T> Mono<T> getSecret(String secretName, Class<T> cls) {
        return this.getSecret(secretName)
                .flatMap((data -> Mono.just(GsonUtils.getInstance().stringToModel(data, cls))))
                .onErrorMap((e) -> new SecretException(e.getMessage()));
    }


    private Mono<String> getSecretValue(String secretName) {
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();

        return Mono.fromFuture(client.getSecretValue(getSecretValueRequest))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new SecretException("Secret value is null"))))
                .onErrorMap(e -> new SecretException(e.getMessage()))
                .flatMap(secretResult -> {
                    if (secretResult.secretString() != null) {
                        String result = secretResult.secretString();
                        return Mono.just(result);
                    }
                    return Mono.error(new SecretException("Secret value is not a String"));
                })
                .doOnError((err) -> {
                    logger.warning("Error retrieving the secret: " + err.getMessage());
                });
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

    private SecretsManagerAsyncClient buildClient(SecretsManagerAsyncClientBuilder builder) {
        builder.credentialsProvider(getProviderChain());
        builder.region(config.getRegion());
        if (!"".equals(config.getEndpoint())) {
            builder.endpointOverride(URI.create(config.getEndpoint()));
        }
        return builder.build();
    }

    private AsyncCache<String, String> initCache() {
        return Caffeine.newBuilder()
                .maximumSize(config.getCacheSize())
                .expireAfterWrite(config.getCacheSeconds(), TimeUnit.SECONDS)
                .buildAsync();
    }

}
