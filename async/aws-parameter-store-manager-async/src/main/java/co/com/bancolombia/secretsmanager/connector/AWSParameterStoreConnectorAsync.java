package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.GenericManagerAsync;
import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.config.AWSParameterStoreConfig;
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
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.SsmAsyncClientBuilder;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public class AWSParameterStoreConnectorAsync implements GenericManagerAsync {

    private final AWSParameterStoreConfig config;
    private final SsmAsyncClient client;
    private final AsyncCache<String, String> cache;
    private final Logger logger = Logger.getLogger("connector.AWSSecretManagerConnector");

    public AWSParameterStoreConnectorAsync(AWSParameterStoreConfig config) {
        this.config = config;
        this.client = buildClient(SsmAsyncClient.builder());
        this.cache = initCache();
    }

    public AWSParameterStoreConnectorAsync(AWSParameterStoreConfig config, SsmAsyncClientBuilder builder) {
        this.config = config;
        this.client = buildClient(builder);
        this.cache = initCache();
    }

    @Override
    public Mono<String> getSecret(String secretName) {
        return Mono.fromFuture(cache.get(secretName,
                (s, executor) -> getSecretValue(secretName).toFuture().toCompletableFuture()));
    }

    @Override
    public <T> Mono<T> getSecret(String secretName, Class<T> cls) {
        return Mono.error(new UnsupportedOperationException("Serialization doesn't apply for parameter store connector"));
    }

    private Mono<String> getSecretValue(String secretName) {
        GetParameterRequest getParameterRequest = GetParameterRequest.builder().name(secretName).build();
        return Mono.fromFuture(client.getParameter(getParameterRequest))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new SecretException("Secret value is null"))))
                .flatMap(secretResult -> {
                    if (secretResult.parameter().value() != null) {
                        String result = secretResult.parameter().value();
                        return Mono.just(result);
                    }
                    return Mono.error(new SecretException("Secret value is not a String"));
                })
                .doOnError((err) -> {
                    logger.warning("Error retrieving the secret: " + err.getMessage());
                });
    }

    private SsmAsyncClient buildClient(SsmAsyncClientBuilder builder) {
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

}
