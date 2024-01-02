package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.commons.utils.GsonUtils;
import co.com.bancolombia.secretsmanager.config.VaultSecretsManagerProperties;
import co.com.bancolombia.secretsmanager.connector.auth.AuthResponse;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonObject;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class VaultAuthenticator {
    private final Logger logger = Logger.getLogger("connector.VaultAuthenticator2");

    private static final String BASE_AUTH_PATH = "/auth/approle/login";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private final HttpClient httpClient;
    private final VaultSecretsManagerProperties properties;
    private final AsyncCache<String, AuthResponse> cache;

    public VaultAuthenticator(HttpClient httpClient, VaultSecretsManagerProperties properties) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.cache = initCache();
    }

    public Mono<AuthResponse> loginByAppRole() {
        if (!properties.roleCredentialsProvided()) {
            return Mono.defer(() ->
                    Mono.error(new SecretException("Could not perform action loginByAppRole. Role id or secret id is null, please check your configuration")));
        } else {
            return Mono.fromFuture(cache.get(properties.getRoleId(),
                    (s, executor) -> performLoginByAppRole().toFuture().toCompletableFuture()));
        }
    }

    private Mono<AuthResponse> performLoginByAppRole() {
        return Mono.fromSupplier(() ->
                        HttpRequest.newBuilder()
                                .uri(URI.create(this.properties.buildUrl() + BASE_AUTH_PATH))
                                .timeout(Duration.ofSeconds(5))
                                .header(CONTENT_TYPE_HEADER, "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(buildLoginBody()))
                                .build()
                )
                .flatMap(request -> Mono.fromFuture(httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())))
                .flatMap(response -> response.statusCode() != 200 ?
                        Mono.error(() -> new SecretException("Error performing authentication with vault: " + response.body())) :
                        Mono.just(response))
                .map(HttpResponse::body)
                .map(body -> GsonUtils.getInstance().stringToModel(body, JsonObject.class))
                .map(map -> map.getAsJsonObject("auth"))
                .map(auth -> AuthResponse.builder()
                        .clientToken(auth.get("client_token").getAsString())
                        .accessor(auth.get("accessor").getAsString())
                        .leaseDuration(auth.get("lease_duration").getAsLong())
                        .renewable(auth.get("renewable").getAsBoolean())
                        .build())
                .doOnSuccess(authResponse -> logger.info("Successfully authenticated by app-role with vault"))
                .doOnError(err -> logger.severe("Error performing authentication with vault: " + err.getMessage()));
    }

    private String buildLoginBody() {
        return "{\"role_id\":\"" + properties.getRoleId() + "\",\"secret_id\":\"" + properties.getSecretId() + "\"}";
    }

    private AsyncCache<String, AuthResponse> initCache() {
        return Caffeine.newBuilder()
                .maximumSize(properties.getAuthCacheProperties().getMaxSize())
                .expireAfterWrite(properties.getAuthCacheProperties().getExpireAfter(), TimeUnit.SECONDS)
                .buildAsync();
    }

}
