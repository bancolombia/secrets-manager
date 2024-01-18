package co.com.bancolombia.secretsmanager.vault.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.File;

/**
 * If you would like to use Vault's TLS Certificate auth backend for client side auth, then you need to provide
 * either:
 * 1) A JKS keystore containing your client-side certificate and private key, and optionally a password.
 * 2) PEM files containing your client-side certificate and private key
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaultKeyStoreProperties {

    // JKS file containing a client certificate and private key.
    private File keyStoreFile;
    // Password for the JKS file or resource (optional).
    private String keyStorePassword;

    // Path to an X.509 certificate in unencrypted PEM format, using UTF-8 encoding.
    private File clientPem;
    // path to an RSA private key in unencrypted PEM format, using UTF-8 encoding.
    private File clientKeyPem;

}
