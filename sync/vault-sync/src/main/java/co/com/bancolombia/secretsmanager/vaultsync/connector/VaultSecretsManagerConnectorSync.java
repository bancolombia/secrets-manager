package co.com.bancolombia.secretsmanager.vaultsync.connector;

import co.com.bancolombia.secretsmanager.api.GenericManager;
import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.commons.utils.GsonUtils;
import co.com.bancolombia.secretsmanager.vaultsync.config.VaultSecretsManagerProperties;
import co.com.bancolombia.secretsmanager.vaultsync.connector.secret.SecretResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Connector to Vault Secrets Manager for reading secrets.
 */
public class VaultSecretsManagerConnectorSync implements GenericManager {

    private static final Logger logger = Logger.getLogger("connector.VaultSecretsManagerConnectorSync");
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String AUTH_HEADER = "X-Vault-Token";
    private static final String ERROR_TEMPLATE = "Error performing operation with vault: %s";


    private final HttpClient httpClient;
    private final VaultSecretsManagerProperties properties;
    private final VaultAuthenticator vaultAuthenticator;
    private final Cache<String, String> cache;

    public VaultSecretsManagerConnectorSync(HttpClient httpClient,
                                            VaultAuthenticator vaultAuthenticator,
                                            VaultSecretsManagerProperties properties) {
        this.httpClient = httpClient;
        this.properties = properties;
        this.vaultAuthenticator = vaultAuthenticator;
        this.cache = initCache();
    }

    @Override
    public String getSecret(String secretName) throws SecretException {
        String secret = cache.getIfPresent(secretName);
        if (secret == null) {
            secret = getSecretValue(secretName);
            cache.put(secretName, secret);
        }
        return secret;
    }

    private String getSecretValue(String secretName) throws SecretException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.properties.buildUrl() +
                        properties.getBaseSecrets() + secretName))
                .timeout(Duration.ofSeconds(5))
                .header(CONTENT_TYPE_HEADER, "application/json")
                .header(AUTH_HEADER, getToken())
                .GET()
                .build();
        try {
            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.statusCode() != 200) {
                throw new SecretException(httpResponse.body());
            } else {
                SecretResponse secretResponse = GsonUtils.getInstance().stringToModel(httpResponse.body(),
                        SecretResponse.class);
                return GsonUtils.getInstance().modelToString(secretResponse.getData().getData());
            }
        } catch (IOException e) {
            throw new SecretException(String.format(ERROR_TEMPLATE, e.getMessage()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SecretException(String.format(ERROR_TEMPLATE, e.getMessage()));
        }
    }

    private String getToken() {
        try {
            return vaultAuthenticator.login().getClientToken();
        } catch (SecretException e) {
            logger.severe("Error retrieving token from vault: " + e.getMessage());
            return "";
        }
    }

    @Override
    public <T> T getSecret(String secretName, Class<T> cls) throws SecretException {
        return GsonUtils.getInstance().stringToModel(this.getSecret(secretName), cls);
    }

    private Cache<String, String> initCache() {
        return Caffeine.newBuilder()
                .maximumSize(properties.getSecretsCacheProperties().getMaxSize())
                .expireAfterWrite(properties.getSecretsCacheProperties().getExpireAfter(), TimeUnit.SECONDS)
                .build();
    }

}
