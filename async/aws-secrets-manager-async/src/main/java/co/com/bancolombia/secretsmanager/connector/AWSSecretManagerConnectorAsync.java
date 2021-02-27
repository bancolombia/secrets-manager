package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.GenericManagerAsync;
import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.config.AWSSecretsManagerConfig;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import reactor.cache.CacheMono;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import co.com.bancolombia.secretsmanager.utils.GsonUtils;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class AWSSecretManagerConnectorAsync implements GenericManagerAsync {

    private final AWSSecretsManagerConfig config;
    private SecretsManagerAsyncClient client;
    private Cache<String,String> cache;
    private Logger logger = Logger.getLogger("connector.AWSSecretManagerConnector");

    public AWSSecretManagerConnectorAsync(AWSSecretsManagerConfig config) {
        this.config = config;
        this.client = buildClient();
        this.cache = initCache();
    }



    @Override
    public Mono<String> getSecret(String secretName)  {
        return CacheMono
                .lookup(secret -> Mono.justOrEmpty(cache.getIfPresent(secret)).map(Signal::next), secretName)
                .onCacheMissResume(()->this.getSecretValue(secretName).subscribeOn(Schedulers.elastic()))
                .andWriteWith(
                        (key, signal) -> Mono.fromRunnable(
                                () -> Optional.ofNullable(signal.get())
                                        .ifPresent(value -> cache.put(key, value))));
    }

    @Override
    public <T> Mono<T> getSecret(String secretName, Class<T> cls) {
        return this.getSecret(secretName)
                .flatMap((data->Mono.just(GsonUtils.getInstance().stringToModel(data, cls))))
                .onErrorMap((e)->new SecretException(e.getMessage()));
    }


    private Mono<String> getSecretValue(String secretName){
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();
        return Mono.fromFuture(client.getSecretValue(getSecretValueRequest))
                .switchIfEmpty(Mono.defer(() -> Mono.error(new SecretException("Secret value is null"))))
                .flatMap(secretResult -> {
                    if (secretResult.secretString() != null) {
                        String result = secretResult.secretString();
                        return Mono.just(result);
                    }
                    return Mono.error(new SecretException("Secret value is not a String"));
                })
                .doOnError((err)->{
                    logger.warning("Error retrieving the secret: "+err.getMessage());
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

    private SecretsManagerAsyncClient buildClient() {
        SecretsManagerAsyncClientBuilder clientBuilder = SecretsManagerAsyncClient.builder()
                .credentialsProvider(getProviderChain())
                .region(config.getRegion());
        if (!config.getEndpoint().equals("")) {
            clientBuilder.endpointOverride(URI.create(config.getEndpoint()));
        }
        return clientBuilder.build();
    }

    private Cache<String,String> initCache(){
       return Caffeine.newBuilder()
                .maximumSize(config.getCacheSize())
                .expireAfterWrite(config.getCacheSeconds(), TimeUnit.SECONDS)
                .build();
    }

}
