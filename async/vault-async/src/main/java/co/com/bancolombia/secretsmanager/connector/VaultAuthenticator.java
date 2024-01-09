package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.commons.utils.GsonUtils;
import co.com.bancolombia.secretsmanager.config.VaultSecretsManagerProperties;
import co.com.bancolombia.secretsmanager.connector.auth.AuthResponse;
import co.com.bancolombia.secretsmanager.connector.auth.K8sAuth;
import co.com.bancolombia.secretsmanager.connector.auth.K8sTokenReader;
import co.com.bancolombia.secretsmanager.connector.auth.RoleAuth;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Class in charge of authenticating with vault
 */
public class VaultAuthenticator {
    private final Logger logger = Logger.getLogger("connector.VaultAuthenticator");
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private final HttpClient httpClient;
    private final VaultSecretsManagerProperties properties;
    private final AsyncCache<String, AuthResponse> cache;
    private final K8sTokenReader k8sTokenReader;
    private final Gson gson = new Gson();
    private final Type mapType = new TypeToken<Map<String, String>>() {}.getType();

    public VaultAuthenticator(HttpClient httpClient,
                              VaultSecretsManagerProperties properties,
                              K8sTokenReader k8sTokenReader) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.k8sTokenReader = k8sTokenReader;
        this.cache = initCache();
    }

    /**
     * Performs the login process with vault. If a token is provided, it will be used. If not, it will try to log in
     * with the role_id and secret_id. If not, it will try to log in with k8s.
     * @return the authentication response with the client token.
     */
    public Mono<AuthResponse> login() {
        return useTokenIfProvided()
                .switchIfEmpty(loginWithRoleId())
                .switchIfEmpty(loginK8s())
                .switchIfEmpty(Mono.defer(() ->
                    Mono.error(new SecretException("Could not perform login with vault. Please check your configuration"))))
                .doOnSuccess(this::checkLeaseDurationAgainstCacheExpTime);
    }

    private Mono<AuthResponse> useTokenIfProvided() {
        return Mono.just(properties.isTokenProvided())
                .filter(c -> c)
                .map(c -> AuthResponse.builder()
                        .clientToken(properties.getToken())
                        .build());
    }

    private Mono<AuthResponse> loginWithRoleId() {
        return Mono.just(properties.isRoleCredentialsProvided())
                .filter(c -> c)
                .flatMap(c -> Mono.fromFuture(cache.get(properties.getRoleId(),
                        (s, executor) -> performLoginByRoleId().toFuture().toCompletableFuture())));
    }

    private Mono<AuthResponse> loginK8s() {
        return Mono.just(properties.isRoleNameForK8sProvided())
                .filter(c -> c)
                .flatMap(c -> Mono.fromFuture(cache.get(properties.getVaultRoleForK8sAuth(),
                        (s, executor) -> performLoginWithK8s().toFuture().toCompletableFuture())));
    }

    private Mono<AuthResponse> performLoginByRoleId() {
        return Mono.fromSupplier(() ->
                        HttpRequest.newBuilder()
                                .uri(URI.create(this.properties.buildUrl() + properties.getAppRoleAuthPath()))
                                .timeout(Duration.ofSeconds(5))
                                .header(CONTENT_TYPE_HEADER, "application/json")
                                .POST(HttpRequest.BodyPublishers.ofString(
                                        gson.toJson(RoleAuth.builder()
                                                .roleId(properties.getRoleId())
                                                .secretId(properties.getSecretId())
                                                .build())
                                ))
                                .build()
                )
                .flatMap(this::doCallAuthApi)
                .doOnSuccess(authResponse ->
                        logger.info("Successfully authenticated via role_id with vault")
                );
    }

    private Mono<AuthResponse> performLoginWithK8s() {
        return k8sTokenReader.getKubernetesServiceAccountToken()
                .flatMap(token -> Mono.fromSupplier(() ->
                                HttpRequest.newBuilder()
                                        .uri(URI.create(this.properties.buildUrl() + properties.getK8sAuthPath()))
                                        .timeout(Duration.ofSeconds(5))
                                        .header(CONTENT_TYPE_HEADER, "application/json")
                                        .POST(HttpRequest.BodyPublishers.ofString(
                                                gson.toJson(K8sAuth.builder()
                                                        .jwt(token)
                                                        .role(properties.getVaultRoleForK8sAuth())
                                                        .build())
                                        ))
                                        .build()
                        ))
                .flatMap(this::doCallAuthApi)
                .doOnSuccess(authResponse ->
                        logger.info("Successfully authenticated via k8s with vault")
                );
    }

    private Mono<AuthResponse> doCallAuthApi(HttpRequest request) {
        return Mono.fromFuture(httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()))
                        .flatMap(response -> response.statusCode() != 200 ?
                                Mono.error(() -> new SecretException("Error performing operation with vault: " + response.body())) :
                                Mono.just(response))
                        .map(HttpResponse::body)
                        .map(body -> GsonUtils.getInstance().stringToModel(body, JsonObject.class))
                        .map(map -> map.getAsJsonObject("auth"))
                        .map(auth -> AuthResponse.builder()
                                .clientToken(auth.get("client_token").getAsString())
                                .accessor(auth.get("accessor").getAsString())
                                .leaseDuration(auth.get("lease_duration").getAsLong())
                                .renewable(auth.get("renewable").getAsBoolean())
                                .metadata(gson.fromJson(auth.get("metadata").toString(), mapType))
                                .build())
                        .doOnError(err -> logger.severe("Error performing operation with vault: " + err.getMessage()));
    }

    private void checkLeaseDurationAgainstCacheExpTime(AuthResponse authResponse) {
        if (!authResponse.isRenewable()) {
            return;
        }
        var leaseInSeconds = authResponse.getLeaseDuration();
        var cacheExpTime = properties.getAuthCacheProperties().getExpireAfter();
        if (cacheExpTime > leaseInSeconds) {
            logger.warning("The configured token cache expiration time is greater " +
                    "than the maximum lease duration of the token. Calling Vault operations using such token, " +
                    "will fail when the token expires. Adjust your cache expiration to be similar to vault's token" +
                    "lease duration!!!");
        }
    }

    private AsyncCache<String, AuthResponse> initCache() {
        return Caffeine.newBuilder()
                .maximumSize(properties.getAuthCacheProperties().getMaxSize())
                .expireAfterWrite(properties.getAuthCacheProperties().getExpireAfter(), TimeUnit.SECONDS)
                .buildAsync();
    }

}
