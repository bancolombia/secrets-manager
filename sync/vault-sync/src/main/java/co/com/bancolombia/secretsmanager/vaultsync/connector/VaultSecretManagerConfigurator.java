package co.com.bancolombia.secretsmanager.vaultsync.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.vault.K8sTokenReader;
import co.com.bancolombia.secretsmanager.vault.VaultSecretManagerConfiguratorBase;
import co.com.bancolombia.secretsmanager.vault.config.VaultSecretsManagerProperties;
import lombok.Builder;

import java.net.http.HttpClient;

/**
 * This class is in charge of configuring the Sync VaultSecretsManagerConnector
 */
public class VaultSecretManagerConfigurator extends VaultSecretManagerConfiguratorBase {

    private final K8sTokenReader k8sTokenReader;

    @Builder(setterPrefix = "with", toBuilder = true)
    public VaultSecretManagerConfigurator(VaultSecretsManagerProperties properties, K8sTokenReader k8sTokenReader) {
        super(properties);
        this.k8sTokenReader = k8sTokenReader;
    }

    /**
     * This method is in charge of configuring the VaultAuthenticator
     * @return the VaultAuthenticator configured.
     * @throws SecretException
     */
    public VaultAuthenticator getVaultAuthenticator() throws SecretException {
        return new VaultAuthenticator(getHttpClient(), getProperties(),
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
                getProperties());
    }

}
