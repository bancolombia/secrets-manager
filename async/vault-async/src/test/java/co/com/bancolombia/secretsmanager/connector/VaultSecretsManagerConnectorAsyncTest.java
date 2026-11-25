package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
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
    void testWrapHttpErrorInSecretException() {
        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(8200)
                .ssl(false)
                .token("hvs.dummy")
                .build();

        IOException ioException = new IOException("Dummy IO Exception");
        HttpClient httpClient = mock(HttpClient.class);
        when(httpClient.sendAsync(Mockito.any(), Mockito.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(CompletableFuture.failedFuture(ioException));
        when(authenticator.login()).thenReturn(Mono.just(AuthResponse.builder().clientToken("hvs.dummy").build()));

        VaultSecretsManagerConnectorAsync connector =
                new VaultSecretsManagerConnectorAsync(httpClient, authenticator, properties);

        StepVerifier.create(connector.getSecret("/path1/foo/bar"))
                .expectErrorMatches(err -> err instanceof SecretException && err.getCause() == ioException)
                .verify();
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
        return """
                {
                  "request_id": "0bdf4c9c-1d24-bbc8-e281-1a09197a87d2",
                  "lease_id": "",
                  "renewable": false,
                  "lease_duration": 0,
                  "data": {
                    "data": {
                      "host": "localhost",
                      "password": "secret",
                      "port": "1234",
                      "user": "jhon"
                    },
                    "metadata": {
                      "created_time": "2023-12-22T03:04:59.26619441Z",
                      "custom_metadata": null,
                      "deletion_time": "",
                      "destroyed": false,
                      "version": 1
                    }
                  },
                  "wrap_info": null,
                  "warnings": null,
                  "auth": null
                }""";
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
