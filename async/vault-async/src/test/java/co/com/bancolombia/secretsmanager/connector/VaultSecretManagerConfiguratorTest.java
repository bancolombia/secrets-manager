package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.config.VaultKeyStoreProperties;
import co.com.bancolombia.secretsmanager.config.VaultSecretsManagerProperties;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;

public class VaultSecretManagerConfiguratorTest {

    @SneakyThrows
    @Test
    public void testClientGeneration() {
        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .sslVerify(false)
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
    public void testClientGenerationWithKeyStore() {
        URI keyStoreUri = getClass().getClassLoader().getResource("keystore.jks").toURI();
        File keyStoreFile = new File(keyStoreUri);

        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .sslVerify(false)
                .host("localhost")
                .port(8200)
                .roleId("x")
                .secretId("y")
                .keyStoreProperties(VaultKeyStoreProperties.builder()
                        .keyStoreFile(keyStoreFile)
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
    public void testClientGenerationWithKeyPem() {
        URI keyUri = getClass().getClassLoader().getResource("client_key.pem").toURI();
        URI certUri = getClass().getClassLoader().getResource("client_cert.pem").toURI();
        File keyFile = new File(keyUri);
        File certFile = new File(certUri);

        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .sslVerify(false)
                .host("localhost")
                .port(8200)
                .roleId("x")
                .secretId("y")
                .keyStoreProperties(VaultKeyStoreProperties.builder()
                        .clientKeyPem(keyFile)
                        .clientPem(certFile)
                        .build()
                )
                .build();

        HttpClient client = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build()
                .getHttpClient();

        Assert.assertNotNull(client);
    }
}
