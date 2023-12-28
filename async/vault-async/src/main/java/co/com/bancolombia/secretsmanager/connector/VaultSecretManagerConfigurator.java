package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.config.VaultKeyStoreProperties;
import co.com.bancolombia.secretsmanager.config.VaultSecretsManagerProperties;
import co.com.bancolombia.secretsmanager.config.VaultTrustStoreProperties;
import co.com.bancolombia.secretsmanager.connector.ssl.SslConfig;
import lombok.Builder;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.logging.Logger;

@Builder(setterPrefix = "with", toBuilder = true)
public class VaultSecretManagerConfigurator {

    private static final Logger logger = Logger.getLogger("config.VaultSecretManagerConfigurator");
    private final VaultSecretsManagerProperties properties;

    public HttpClient getHttpClient() throws SecretException {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(properties.getHttpProperties().getConnectionTimeout()));
        if (properties.isSsl() || properties.getTrustStoreProperties() != null || properties.getKeyStoreProperties() != null) {
            clientBuilder = clientBuilder.sslContext(buildSslConfig().getSslContext());
        }
        return clientBuilder.build();
    }

    public VaultAuthenticator getVaultAuthenticator() throws SecretException {
        return new VaultAuthenticator(getHttpClient(), properties);
    }

    public VaultSecretsManagerConnectorAsync getVaultClient() throws SecretException {
        HttpClient httpClient = getHttpClient();
        return new VaultSecretsManagerConnectorAsync(httpClient,
                new VaultAuthenticator(httpClient, properties),
                properties);
    }

    private SslConfig buildSslConfig() throws SecretException {
        SslConfig sslConfig = new SslConfig();
        if (properties.getTrustStoreProperties() != null) {
            setTrustConfiguration(sslConfig, properties.getTrustStoreProperties());
        }
        if (properties.getKeyStoreProperties() != null) {
            setKeystoreConfiguration(sslConfig, properties.getKeyStoreProperties());
        }
        return sslConfig.build();
    }

    private SslConfig setTrustConfiguration(SslConfig sslConfig,
                                            VaultTrustStoreProperties trustStoreProperties) throws SecretException {
        if (trustStoreProperties.getTrustStoreJksFile() != null) {
            sslConfig.trustStoreFile(trustStoreProperties.getTrustStoreJksFile());
        }  else if (trustStoreProperties.getPemFile() != null) {
            sslConfig.pemFile(trustStoreProperties.getPemFile());
        } else {
            logger.warning("No trust store file or pem resource provided");
        }
        return sslConfig;
    }

    private SslConfig setKeystoreConfiguration(SslConfig sslConfig,
                                               VaultKeyStoreProperties keyStoreProperties) throws SecretException {
        if (keyStoreProperties.getKeyStoreFile() != null) {
            sslConfig.keyStoreFile(keyStoreProperties.getKeyStoreFile(),
                    keyStoreProperties.getKeyStorePassword());
        } else if (keyStoreProperties.getClientPem() != null && keyStoreProperties.getClientKeyPem() != null) {
            sslConfig.clientPemFile(keyStoreProperties.getClientPem());
            sslConfig.clientKeyPemFile(keyStoreProperties.getClientKeyPem());
        } else {
            logger.warning("No key store file or pem resources provided");
        }
        return sslConfig;
    }
}
