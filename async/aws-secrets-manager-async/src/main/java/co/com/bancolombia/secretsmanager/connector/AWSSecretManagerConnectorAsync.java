package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.GenericManagerAsync;
import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.commons.utils.GsonUtils;
import co.com.bancolombia.secretsmanager.config.AWSSecretsManagerConfig;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
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
                .onErrorMap(e -> !(e instanceof SecretException), e -> new SecretException(e.getMessage(), e));
    }


    private Mono<String> getSecretValue(String secretName) {
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();

        return Mono.fromFuture(() -> client.getSecretValue(getSecretValueRequest))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new SecretException("Secret value is null"))))
                .flatMap(secretResult -> {
                    if (secretResult.secretString() != null) {
                        String result = secretResult.secretString();
                        return Mono.just(result);
                    }
                    return Mono.error(new SecretException("Secret value is not a String"));
                })
                .onErrorMap(e -> !(e instanceof SecretException), e -> new SecretException(e.getMessage(), e))
                .doOnError(err -> logger.warning("Error retrieving the secret: " + err.getMessage()));
    }

    /**
     * Builds the AWS client using the AWS SDK default credentials provider chain
     * ({@link DefaultCredentialsProvider}).
     */
    private SecretsManagerAsyncClient buildClient(SecretsManagerAsyncClientBuilder builder) {
        builder.credentialsProvider(DefaultCredentialsProvider.builder().build());
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
