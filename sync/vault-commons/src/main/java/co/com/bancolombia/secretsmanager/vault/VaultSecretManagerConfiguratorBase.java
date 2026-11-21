package co.com.bancolombia.secretsmanager.vault;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.vault.config.VaultKeyStoreProperties;
import co.com.bancolombia.secretsmanager.vault.config.VaultSecretsManagerProperties;
import co.com.bancolombia.secretsmanager.vault.config.VaultTrustStoreProperties;
import co.com.bancolombia.secretsmanager.vault.ssl.SslConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * This is a base class for the VaultSecretsManagerConnector
 */
@Getter
@RequiredArgsConstructor
public class VaultSecretManagerConfiguratorBase {

    protected final VaultSecretsManagerProperties properties;

    /**
     * This method is in charge of configuring the HttpClient
     * @return HttpClient configured.
     * @throws SecretException if the SSL/TLS configuration (trust store or key store) cannot be loaded
     */
    public HttpClient getHttpClient() throws SecretException {
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(properties.getHttpProperties().getConnectionTimeout()));
        if (properties.isSsl() || properties.getTrustStoreProperties() != null || properties.getKeyStoreProperties() != null) {
            clientBuilder = clientBuilder.sslContext(buildSslConfig().getSslContext());
        }
        return clientBuilder.build();
    }

    /**
     * Builds the SSL configuration from the configured trust store and key store properties.
     * @return the SslConfig built.
     * @throws SecretException if the SSL/TLS configuration (trust store or key store) cannot be loaded
     */
    protected SslConfig buildSslConfig() throws SecretException {
        SslConfig sslConfig = new SslConfig();
        if (properties.getTrustStoreProperties() != null) {
            setTrustConfiguration(sslConfig, properties.getTrustStoreProperties());
        }
        if (properties.getKeyStoreProperties() != null) {
            setKeystoreConfiguration(sslConfig, properties.getKeyStoreProperties());
        }
        return sslConfig.build();
    }

    /**
     * Applies the trust store (JKS or PEM) to the given SslConfig.
     * @return the SslConfig updated.
     * @throws SecretException if no trust store file or PEM is provided, or it cannot be read
     */
    protected SslConfig setTrustConfiguration(SslConfig sslConfig,
                                            VaultTrustStoreProperties trustStoreProperties) throws SecretException {
        if (trustStoreProperties.getTrustStoreJksFile() != null) {
            sslConfig.trustStoreFile(trustStoreProperties.getTrustStoreJksFile());
        }  else if (trustStoreProperties.getPemFile() != null) {
            sslConfig.pemFile(trustStoreProperties.getPemFile());
        } else {
            throw new SecretException("VaultTrustStoreProperties was set, but no trust store file or pem resource provided");
        }
        return sslConfig;
    }

    /**
     * Applies the key store (JKS or client PEM certificate and key) to the given SslConfig.
     * @return the SslConfig updated.
     * @throws SecretException if no key store file or PEM resources are provided, or they cannot be read
     */
    protected SslConfig setKeystoreConfiguration(SslConfig sslConfig,
                                               VaultKeyStoreProperties keyStoreProperties) throws SecretException {
        if (keyStoreProperties.getKeyStoreFile() != null) {
            sslConfig.keyStoreFile(keyStoreProperties.getKeyStoreFile(),
                    keyStoreProperties.getKeyStorePassword());
        } else if (keyStoreProperties.getClientPem() != null && keyStoreProperties.getClientKeyPem() != null) {
            sslConfig.clientPemFile(keyStoreProperties.getClientPem());
            sslConfig.clientKeyPemFile(keyStoreProperties.getClientKeyPem());
        } else {
            throw new SecretException("VaultKeyStoreProperties was set, but no key store file or pem resources provided");
        }
        return sslConfig;
    }
}
