package co.com.bancolombia.secretsmanager.vaultsync.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.vault.config.VaultKeyStoreProperties;
import co.com.bancolombia.secretsmanager.vault.config.VaultSecretsManagerProperties;
import co.com.bancolombia.secretsmanager.vault.config.VaultTrustStoreProperties;
import co.com.bancolombia.secretsmanager.vault.ssl.SslConfig;
import lombok.Builder;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * This class is in charge of configuring the VaultSecretsManagerConnector
 */
@Builder(setterPrefix = "with", toBuilder = true)
public class VaultSecretManagerConfigurator {

    private static final Logger logger = Logger.getLogger("config.VaultSecretManagerConfigurator");
    private final VaultSecretsManagerProperties properties;
    private final K8sTokenReader k8sTokenReader;

    /**
     * This method is in charge of configuring the HttpClient
     * @return HttpClient configured.
     * @throws SecretException
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
     * This method is in charge of configuring the VaultAuthenticator
     * @return the VaultAuthenticator configured.
     * @throws SecretException
     */
    public VaultAuthenticator getVaultAuthenticator() throws SecretException {
        return new VaultAuthenticator(getHttpClient(), properties,
                k8sTokenReader != null? k8sTokenReader : new K8sTokenReader());
    }

    /**
     * This method is in charge of configuring the VaultSecretsManagerConnector
     * @return the VaultSecretsManagerConnector configured.
     * @throws SecretException
     */
    public VaultSecretsManagerConnectorSync getVaultClient() throws SecretException {
        HttpClient httpClient = getHttpClient();
        return new VaultSecretsManagerConnectorSync(httpClient,
                getVaultAuthenticator(),
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
            throw new SecretException("VaultTrustStoreProperties was set, but no trust store file or pem resource provided");
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
            throw new SecretException("VaultKeyStoreProperties was set, but no key store file or pem resources provided");
        }
        return sslConfig;
    }
}
