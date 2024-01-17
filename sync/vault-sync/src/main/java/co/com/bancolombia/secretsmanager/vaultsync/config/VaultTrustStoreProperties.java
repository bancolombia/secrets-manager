package co.com.bancolombia.secretsmanager.vaultsync.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.File;

/**
 * Provide information about a JKS truststore, containing Vault's server-side certificate for basic SSL, using one of
 * the following two options:
 * 1) A JKS file with trustStoreJksFile attribute or
 * 2) Pem files using pemFile attribute
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaultTrustStoreProperties {

    /**
     * JKS file containing Vault server cert(s) that can be trusted.
     */
    private File trustStoreJksFile;

    /**
     * Supply the path to an X.509 certificate in unencrypted PEM format, using UTF-8 encoding
     * (defaults to "VAULT_SSL_CERT" environment variable)
     */
    private File pemFile;


}
