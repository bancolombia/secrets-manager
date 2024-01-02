package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.GenericManagerAsync;
import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.commons.utils.GsonUtils;
import co.com.bancolombia.secretsmanager.config.VaultSecretsManagerProperties;
import co.com.bancolombia.secretsmanager.connector.auth.AuthResponse;
import co.com.bancolombia.secretsmanager.connector.secret.SecretResponse;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class VaultSecretsManagerConnectorAsync implements GenericManagerAsync {

    private static final Logger logger = Logger.getLogger("connector.VaultSecretsManagerConnectorAsync");
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String AUTH_HEADER = "X-Vault-Token";

    private final HttpClient httpClient;
    private final VaultSecretsManagerProperties properties;
    private final VaultAuthenticator vaultAuthenticator;
    private final AsyncCache<String, String> cache;

    public VaultSecretsManagerConnectorAsync(HttpClient httpClient,
                                             VaultAuthenticator vaultAuthenticator,
                                             VaultSecretsManagerProperties properties) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.vaultAuthenticator = vaultAuthenticator;
        this.cache = initCache();
    }

    @Override
    public Mono<String> getSecret(String secretName) throws SecretException {
        return Mono.fromFuture(cache.get(secretName,
                (s, executor) -> getSecretValue(secretName).toFuture().toCompletableFuture()));
    }

    private Mono<String> getSecretValue(String secretName) {
        return getToken()
                .map(token ->
                        HttpRequest.newBuilder()
                                .uri(URI.create(this.properties.buildUrl() +
                                        properties.getBaseSecrets() + secretName))
                                .timeout(Duration.ofSeconds(5))
                                .header(CONTENT_TYPE_HEADER, "application/json")
                                .header(AUTH_HEADER, token)
                                .GET()
                                .build()
                )
                .flatMap(request -> Mono.fromFuture(httpClient.sendAsync(request,
                        HttpResponse.BodyHandlers.ofString())))
                .flatMap(httpResponse -> {
                    if (httpResponse.statusCode() != 200) {
                        return Mono.error(() -> new SecretException(httpResponse.body()));
                    } else {
                        return Mono.just(GsonUtils.getInstance().stringToModel(httpResponse.body(),
                                SecretResponse.class));
                    }
                })
                .map(data -> GsonUtils.getInstance().modelToString(data.getData().getData()))
                .doOnError(err -> logger.severe("Error retrieving secret from vault: " + err.getMessage()));
    }

    private Mono<String> getToken() {
        return Mono.defer(() -> {
            if (this.properties.getToken() != null) {
                return Mono.just(this.properties.getToken());
            } else {
                return vaultAuthenticator.loginByAppRole()
                        .map(AuthResponse::getClientToken);
            }
        });
    }

    @Override
    public <T> Mono<T> getSecret(String secretName, Class<T> cls) throws SecretException {
        return this.getSecret(secretName)
                .map(secret -> GsonUtils.getInstance().stringToModel(secret, cls));
    }

    private AsyncCache<String, String> initCache() {
        return Caffeine.newBuilder()
                .maximumSize(properties.getSecretsCacheProperties().getMaxSize())
                .expireAfterWrite(properties.getSecretsCacheProperties().getExpireAfter(), TimeUnit.SECONDS)
                .buildAsync();
    }

}
