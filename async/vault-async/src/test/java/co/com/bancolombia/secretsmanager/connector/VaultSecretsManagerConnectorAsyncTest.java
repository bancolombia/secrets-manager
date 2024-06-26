package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.vault.auth.AuthResponse;
import co.com.bancolombia.secretsmanager.vault.config.VaultSecretsManagerProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.http.HttpClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaultSecretsManagerConnectorAsyncTest {

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

        when(authenticator.login()).thenReturn(Mono.just(AuthResponse.builder().clientToken("hvs.dummy").build()));

        VaultSecretsManagerConnectorAsync vaultSecretsManagerConnectorAsync =
                new VaultSecretsManagerConnectorAsync(httpClient, authenticator, properties);

        StepVerifier.create(vaultSecretsManagerConnectorAsync.getSecret("/path1/foo/bar"))
                .expectSubscription()
                .expectNextMatches(secret -> secret.contains("password")
                        && secret.contains("secret")
                        && secret.contains("port")
                        && secret.contains("1234")
                        && secret.contains("host")
                        && secret.contains("localhost")
                        && secret.contains("user")
                        && secret.contains("jhon"))
                .verifyComplete();

        assertEquals("/v1/kv/data//path1/foo/bar", server.takeRequest().getPath());

        server.shutdown();
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

        VaultSecretsManagerConnectorAsync vaultSecretsManagerConnectorAsync =
                configurator.getVaultClient();

        StepVerifier.create(vaultSecretsManagerConnectorAsync.getSecret("/path1/foo/bar"))
                .expectSubscription()
                .expectNextMatches(secret -> secret.contains("password")
                        && secret.contains("secret")
                        && secret.contains("port")
                        && secret.contains("1234")
                        && secret.contains("host")
                        && secret.contains("localhost")
                        && secret.contains("user")
                        && secret.contains("jhon"))
                .verifyComplete();

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

        when(authenticator.login()).thenReturn(Mono.just(AuthResponse.builder().clientToken("hvs.dummy").build()));

        VaultSecretsManagerConnectorAsync vaultSecretsManagerConnectorAsync =
                new VaultSecretsManagerConnectorAsync(httpClient, authenticator, properties);

        StepVerifier.create(vaultSecretsManagerConnectorAsync.getSecret("/path1/foo/bar", SamplePojo.class))
                .expectSubscription()
                .expectNextMatches(pojo -> pojo.getUser().equals("jhon")
                        && pojo.getHost().equals("localhost")
                        && pojo.getPort().equals("1234")
                        && pojo.getPassword().equals("secret"))
                .verifyComplete();

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
