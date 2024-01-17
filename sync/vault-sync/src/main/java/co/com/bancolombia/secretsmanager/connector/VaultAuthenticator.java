package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.commons.utils.GsonUtils;
import co.com.bancolombia.secretsmanager.config.VaultSecretsManagerProperties;
import co.com.bancolombia.secretsmanager.connector.auth.AuthResponse;
import co.com.bancolombia.secretsmanager.connector.auth.K8sAuth;
import co.com.bancolombia.secretsmanager.connector.auth.K8sTokenReader;
import co.com.bancolombia.secretsmanager.connector.auth.RoleAuth;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
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

    private static final String ERROR_TEMPLATE = "Error performing operation with vault: %s";

    private final HttpClient httpClient;
    private final VaultSecretsManagerProperties properties;
    private final Cache<String, AuthResponse> cache;
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
    public AuthResponse login() throws SecretException {
        AuthResponse response = useTokenIfProvided();
        if (response == null) {
            response = loginWithRoleId();
        }
        if (response == null) {
            response = loginK8s();
        }
        if (response != null) {
            checkLeaseDurationAgainstCacheExpTime(response);
        } else {
            throw new SecretException("Could not perform login with vault. Please check your configuration");
        }
        return response;
    }

    private AuthResponse useTokenIfProvided() {
        if (properties.isTokenProvided()) {
            return AuthResponse.builder()
                    .clientToken(properties.getToken())
                    .build();
        }
        return null;
    }

    private AuthResponse loginWithRoleId() throws SecretException {
        AuthResponse response = null;
        if (properties.isRoleCredentialsProvided()) {
            response = cache.getIfPresent(properties.getRoleId());
            if (response == null) {
                response = performLoginByRoleId();
                cache.put(properties.getRoleId(), response);
            }
        }
        return response;
    }

    private AuthResponse loginK8s() throws SecretException {
        AuthResponse response = null;
        if (properties.isRoleNameForK8sProvided()) {
            response = cache.getIfPresent(properties.getVaultRoleForK8sAuth());
            if (response == null) {
                response = performLoginWithK8s();
                cache.put(properties.getVaultRoleForK8sAuth(), response);
            }
        }
        return response;
    }

    private AuthResponse performLoginByRoleId() throws SecretException {
        return doCallAuthApi(
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
        );
    }

    private AuthResponse performLoginWithK8s() throws SecretException {
        String k8sToken = k8sTokenReader.getKubernetesServiceAccountToken();
        return doCallAuthApi(
                HttpRequest.newBuilder()
                        .uri(URI.create(this.properties.buildUrl() + properties.getK8sAuthPath()))
                        .timeout(Duration.ofSeconds(5))
                        .header(CONTENT_TYPE_HEADER, "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(
                                gson.toJson(K8sAuth.builder()
                                        .jwt(k8sToken)
                                        .role(properties.getVaultRoleForK8sAuth())
                                        .build())
                        ))
                        .build()
        );
    }

    private AuthResponse doCallAuthApi(HttpRequest request) throws SecretException {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new SecretException(String.format(ERROR_TEMPLATE, response.body()));
            }
            JsonObject bodyJson = GsonUtils.getInstance().stringToModel(response.body(), JsonObject.class);
            JsonObject authPart = bodyJson.getAsJsonObject("auth");
            return AuthResponse.builder()
                    .clientToken(authPart.get("client_token").getAsString())
                    .accessor(authPart.get("accessor").getAsString())
                    .leaseDuration(authPart.get("lease_duration").getAsLong())
                    .renewable(authPart.get("renewable").getAsBoolean())
                    .metadata(gson.fromJson(authPart.get("metadata").toString(), mapType))
                    .build();
        } catch (IOException e) {
            throw new SecretException(String.format(ERROR_TEMPLATE, e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SecretException(String.format(ERROR_TEMPLATE, e.getMessage()));
        }
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

    private Cache<String, AuthResponse> initCache() {
        return Caffeine.newBuilder()
                .maximumSize(properties.getAuthCacheProperties().getMaxSize())
                .expireAfterWrite(properties.getAuthCacheProperties().getExpireAfter(), TimeUnit.SECONDS)
                .build();
    }

}
