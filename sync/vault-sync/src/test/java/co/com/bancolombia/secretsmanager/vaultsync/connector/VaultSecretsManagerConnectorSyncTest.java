package co.com.bancolombia.secretsmanager.vaultsync.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.vault.auth.AuthResponse;
import co.com.bancolombia.secretsmanager.vault.config.VaultSecretsManagerProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaultSecretsManagerConnectorSyncTest {

    @Mock
    VaultAuthenticator authenticator;

    @SneakyThrows
    @Test
    void testGetSecretContent() {

        MockWebServer server = new MockWebServer();

        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(secretPayload());
        server.enqueue(response);
        server.start();

        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(server.getPort())
                .ssl(false)
                .roleId("65903d42-6dd4-2aa3-6a61-xxxxxxxxxx")
                .secretId("0cce6d0b-e756-c12e-9729-xxxxxxxxx")
                .build();

        VaultSecretManagerConfigurator configurator = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build();

        HttpClient httpClient = configurator.getHttpClient();

        when(authenticator.login()).thenReturn(AuthResponse.builder().clientToken("hvs.dummy").build());

        VaultSecretsManagerConnectorSync vaultSecretsManagerConnectorSync =
                new VaultSecretsManagerConnectorSync(httpClient, authenticator, properties);

        String secret = vaultSecretsManagerConnectorSync.getSecret("/path1/foo/bar");
        assertNotNull(secret);
        assertTrue(secret.contains("password")
                && secret.contains("secret")
                && secret.contains("port")
                && secret.contains("1234")
                && secret.contains("host")
                && secret.contains("localhost")
                && secret.contains("user")
                && secret.contains("jhon"));

        assertEquals("/v1/kv/data//path1/foo/bar", server.takeRequest().getPath());

        server.shutdown();
    }

    @SneakyThrows
    @Test
    void testUnsuccessfulGetSecretContent() {

        MockWebServer server = new MockWebServer();

        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("Bad Request");
        server.enqueue(response);
        server.start();

        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(server.getPort())
                .ssl(false)
                .roleId("65903d42-6dd4-2aa3-6a61-xxxxxxxxxx")
                .secretId("0cce6d0b-e756-c12e-9729-xxxxxxxxx")
                .build();

        VaultSecretManagerConfigurator configurator = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build();

        HttpClient httpClient = configurator.getHttpClient();

        when(authenticator.login()).thenThrow(new SecretException("Dummy Exception"));

        VaultSecretsManagerConnectorSync vaultSecretsManagerConnectorSync =
                new VaultSecretsManagerConnectorSync(httpClient, authenticator, properties);

        assertThrows(SecretException.class,
                () -> vaultSecretsManagerConnectorSync.getSecret("/path1/foo/bar"));

        assertEquals("/v1/kv/data//path1/foo/bar", server.takeRequest().getPath());

        server.shutdown();
    }

    @SneakyThrows
    @Test
    void testHandleIOException() {

        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(8200)
                .ssl(false)
                .roleId("65903d42-6dd4-2aa3-6a61-xxxxxxxxxx")
                .secretId("0cce6d0b-e756-c12e-9729-xxxxxxxxx")
                .build();

        VaultSecretManagerConfigurator configurator = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build();

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        when(httpClient.send(Mockito.any(), Mockito.any())).thenThrow(new IOException("Dummy IO Exception"));
        when(authenticator.login()).thenReturn(AuthResponse.builder().clientToken("hvs.dummy").build());

        VaultSecretsManagerConnectorSync vaultSecretsManagerConnectorSync =
                new VaultSecretsManagerConnectorSync(httpClient, authenticator, properties);

        assertThrows(SecretException.class,
                () -> vaultSecretsManagerConnectorSync.getSecret("/path1/foo/bar"));

    }

    @SneakyThrows
    @Test
    void testHandleInterruptedException() {

        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(8200)
                .ssl(false)
                .roleId("65903d42-6dd4-2aa3-6a61-xxxxxxxxxx")
                .secretId("0cce6d0b-e756-c12e-9729-xxxxxxxxx")
                .build();

        VaultSecretManagerConfigurator configurator = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build();

        HttpClient httpClient = Mockito.mock(HttpClient.class);
        when(httpClient.send(Mockito.any(), Mockito.any())).thenThrow(new InterruptedException("Dummy Interrupted Exception"));
        when(authenticator.login()).thenReturn(AuthResponse.builder().clientToken("hvs.dummy").build());

        VaultSecretsManagerConnectorSync vaultSecretsManagerConnectorSync =
                new VaultSecretsManagerConnectorSync(httpClient, authenticator, properties);

        assertThrows(SecretException.class,
                () -> vaultSecretsManagerConnectorSync.getSecret("/path1/foo/bar"));

    }

    @SneakyThrows
    @Test
    void testGetSecretContentNoAuthUseTokenProvided() {

        MockWebServer server = new MockWebServer();

        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(secretPayload());
        server.enqueue(response);
        server.start();

        VaultSecretManagerConfigurator configurator = VaultSecretManagerConfigurator.builder()
                .withProperties(VaultSecretsManagerProperties.builder()
                        .host("localhost")
                        .port(server.getPort())
                        .ssl(false)
                        .token("shv.xxxxxxxxxx")
                        .build())
                .build();

        VaultSecretsManagerConnectorSync vaultSecretsManagerConnectorSync =
                configurator.getVaultClient();

        String secret = vaultSecretsManagerConnectorSync.getSecret("/path1/foo/bar");
        assertNotNull(secret);
        assertTrue(secret.contains("password")
                && secret.contains("secret")
                && secret.contains("port")
                && secret.contains("1234")
                && secret.contains("host")
                && secret.contains("localhost")
                && secret.contains("user")
                && secret.contains("jhon"));

        assertEquals("/v1/kv/data//path1/foo/bar", server.takeRequest().getPath());

        server.shutdown();
    }

    @SneakyThrows
    @Test
    void testGetSecretPojo() {

        MockWebServer server = new MockWebServer();

        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200)
                .setBody(secretPayload());
        server.enqueue(response);
        server.start();

        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(server.getPort())
                .ssl(false)
                .roleId("65903d42-6dd4-2aa3-6a61-xxxxxxxxxx")
                .secretId("0cce6d0b-e756-c12e-9729-xxxxxxxxx")
                .build();

        VaultSecretManagerConfigurator configurator = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build();

        HttpClient httpClient = configurator.getHttpClient();

        when(authenticator.login()).thenReturn(AuthResponse.builder().clientToken("hvs.dummy").build());

        VaultSecretsManagerConnectorSync vaultSecretsManagerConnectorSync =
                new VaultSecretsManagerConnectorSync(httpClient, authenticator, properties);

        SamplePojo pojo = vaultSecretsManagerConnectorSync.getSecret("/path1/foo/bar", SamplePojo.class);
        assertNotNull(pojo);
        assertTrue(pojo.getUser().equals("jhon")
                && pojo.getHost().equals("localhost")
                && pojo.getPort().equals("1234")
                && pojo.getPassword().equals("secret"));

        assertEquals("/v1/kv/data//path1/foo/bar", server.takeRequest().getPath());

        server.shutdown();
    }

    private String secretPayload() {
        return "{\n" +
                "  \"request_id\": \"0bdf4c9c-1d24-bbc8-e281-1a09197a87d2\",\n" +
                "  \"lease_id\": \"\",\n" +
                "  \"renewable\": false,\n" +
                "  \"lease_duration\": 0,\n" +
                "  \"data\": {\n" +
                "    \"data\": {\n" +
                "      \"host\": \"localhost\",\n" +
                "      \"password\": \"secret\",\n" +
                "      \"port\": \"1234\",\n" +
                "      \"user\": \"jhon\"\n" +
                "    },\n" +
                "    \"metadata\": {\n" +
                "      \"created_time\": \"2023-12-22T03:04:59.26619441Z\",\n" +
                "      \"custom_metadata\": null,\n" +
                "      \"deletion_time\": \"\",\n" +
                "      \"destroyed\": false,\n" +
                "      \"version\": 1\n" +
                "    }\n" +
                "  },\n" +
                "  \"wrap_info\": null,\n" +
                "  \"warnings\": null,\n" +
                "  \"auth\": null\n" +
                "}";
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class SamplePojo {
        private String password;
        private String host;
        private String user;
        private String port;
    }
}
