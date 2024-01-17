package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.config.VaultKeyStoreProperties;
import co.com.bancolombia.secretsmanager.config.VaultSecretsManagerProperties;
import co.com.bancolombia.secretsmanager.config.VaultTrustStoreProperties;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;

public class VaultSecretManagerConfiguratorTest {

    @SneakyThrows
    @Test
    public void testHttpClientGeneration() {
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

        Assert.assertNotNull(client);
    }

    @SneakyThrows
    @Test
    public void testVaultClientGeneration() {
        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(8200)
                .roleId("x")
                .secretId("y")
                .build();

        VaultSecretsManagerConnectorSync client = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build()
                .getVaultClient();

        Assert.assertNotNull(client);
    }

    @SneakyThrows
    @Test
    public void testClientGenerationWithKeyStore() {
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

        Assert.assertNotNull(client);
    }

    @SneakyThrows
    @Test
    public void testClientGenerationWithKeyStoreNoValues() {
        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(8200)
                .roleId("x")
                .secretId("y")
                .keyStoreProperties(VaultKeyStoreProperties.builder()
                        .build()
                )
                .build();

        Assert.assertThrows(SecretException.class, () -> VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build()
                .getHttpClient());

    }

    @SneakyThrows
    @Test
    public void testClientGenerationWithTrustStore() {
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

        Assert.assertNotNull(client);
    }

    @SneakyThrows
    @Test
    public void testClientGenerationWithTrustPem() {
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

        Assert.assertNotNull(client);
    }

    @SneakyThrows
    @Test
    public void testClientGenerationWithTrustNoValues() {
        URI pemUri = getClass().getClassLoader().getResource("certificate.arm").toURI();
        File pemFile = new File(pemUri);

        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(8200)
                .roleId("x")
                .secretId("y")
                .trustStoreProperties(VaultTrustStoreProperties.builder()
                        .build()
                )
                .build();

        Assert.assertThrows(SecretException.class, () -> VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build()
                .getHttpClient());
    }

}
