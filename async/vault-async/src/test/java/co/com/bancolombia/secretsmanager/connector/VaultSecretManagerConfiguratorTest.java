package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.vault.config.VaultKeyStoreProperties;
import co.com.bancolombia.secretsmanager.vault.config.VaultSecretsManagerProperties;
import co.com.bancolombia.secretsmanager.vault.config.VaultTrustStoreProperties;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VaultSecretManagerConfiguratorTest {

    @SneakyThrows
    @Test
    void testHttpClientGeneration() {
        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(8200)
                .roleId("x")
                .secretId("y")
                .build();

        HttpClient client = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build()
                .getHttpClient();

        assertNotNull(client);
    }

    @SneakyThrows
    @Test
    void testVaultClientGeneration() {
        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(8200)
                .roleId("x")
                .secretId("y")
                .build();

        VaultSecretsManagerConnectorAsync client = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build()
                .getVaultClient();

        assertNotNull(client);
    }

    @SneakyThrows
    @Test
    void testClientGenerationWithKeyStore() {
        URI keyStoreUri = getClass().getClassLoader().getResource("keystore.jks").toURI();
        File keyStoreFile = new File(keyStoreUri);

        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(8200)
                .roleId("x")
                .secretId("y")
                .keyStoreProperties(VaultKeyStoreProperties.builder()
                        .keyStoreFile(keyStoreFile)
                        .keyStorePassword("changeit")
                        .build()
                )
                .build();

        HttpClient client = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build()
                .getHttpClient();

        assertNotNull(client);
    }

    @SneakyThrows
    @Test
    void testClientGenerationWithKeyStoreNoValues() {
        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(8200)
                .roleId("x")
                .secretId("y")
                .keyStoreProperties(VaultKeyStoreProperties.builder()
                        .build()
                )
                .build();

        assertThrows(SecretException.class, () -> VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build()
                .getHttpClient());

    }

    @SneakyThrows
    @Test
    void testClientGenerationWithTrustStore() {
        URI storeUri = getClass().getClassLoader().getResource("truststore.jks").toURI();
        File storeFile = new File(storeUri);

        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(8200)
                .roleId("x")
                .secretId("y")
                .trustStoreProperties(VaultTrustStoreProperties.builder()
                        .trustStoreJksFile(storeFile)
                        .build()
                )
                .build();

        HttpClient client = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build()
                .getHttpClient();

        assertNotNull(client);
    }

    @SneakyThrows
    @Test
    void testClientGenerationWithTrustPem() {
        URI pemUri = getClass().getClassLoader().getResource("certificate.arm").toURI();
        File pemFile = new File(pemUri);

        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(8200)
                .roleId("x")
                .secretId("y")
                .trustStoreProperties(VaultTrustStoreProperties.builder()
                        .pemFile(pemFile)
                        .build()
                )
                .build();

        HttpClient client = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build()
                .getHttpClient();

        assertNotNull(client);
    }

    @SneakyThrows
    @Test
    void testClientGenerationWithTrustNoValues() {
        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(8200)
                .roleId("x")
                .secretId("y")
                .trustStoreProperties(VaultTrustStoreProperties.builder()
                        .build()
                )
                .build();

        assertThrows(SecretException.class, () -> VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build()
                .getHttpClient());
    }

}
