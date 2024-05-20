package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.vault.K8sTokenReader;
import co.com.bancolombia.secretsmanager.vault.config.VaultSecretsManagerProperties;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaulAuthenticatorTest {

    @SneakyThrows
    @Test
    void testAuthenticateWithRoleIdAndSecretId() {
        MockWebServer server = new MockWebServer();

        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(buildSuccessAuthResponse());
        server.enqueue(response);
        server.start();

        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(server.getPort())
                .roleId("65903d42-6dd4-2aa3-6a61-xxxxxxxxxx")
                .secretId("0cce6d0b-e756-c12e-9729-xxxxxxxxxx")
                .build();

        VaultSecretManagerConfigurator configurator = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build();

        VaultAuthenticator vaultAuthenticator = configurator.getVaultAuthenticator();

        StepVerifier.create(vaultAuthenticator.login())
                .expectSubscription()
                .expectNextMatches(authResponse -> {
                    assertEquals("hvs.dummytoken", authResponse.getClientToken());
                    return true;
                })
                .verifyComplete();

        assertEquals("/v1/auth/approle/login", server.takeRequest().getPath());

        server.shutdown();
    }

    @SneakyThrows
    @Test
    void testAuthenticateWithK8s() {

        MockWebServer server = new MockWebServer();

        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setBody(buildSuccessAuthResponse());
        server.enqueue(response);
        server.start();

        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(server.getPort())
                .token(null)
                .vaultRoleForK8sAuth("xxxxxxxxxx")
                .build();

        K8sTokenReader k8sTokenReaderMock = Mockito.mock(K8sTokenReader.class);
        when(k8sTokenReaderMock.getKubernetesServiceAccountToken()).thenReturn("ey...");

        VaultSecretManagerConfigurator configurator = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .withK8sTokenReader(k8sTokenReaderMock)
                .build();

        VaultAuthenticator vaultAuthenticator = configurator.getVaultAuthenticator();

        StepVerifier.create(vaultAuthenticator.login())
                .expectSubscription()
                .expectNextMatches(authResponse -> {
                    assertEquals("hvs.dummytoken", authResponse.getClientToken());
                    return true;
                })
                .verifyComplete();

        assertEquals("/v1/auth/kubernetes/login", server.takeRequest().getPath());

        server.shutdown();
    }

    @SneakyThrows
    @Test
    void testHandleNoCredentials() {
        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .host("localhost")
                .port(2020)
                .token(null)
                .roleId(null)
                .secretId(null)
                .vaultRoleForK8sAuth(null)
                .build();

        VaultSecretManagerConfigurator configurator = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build();

        VaultAuthenticator vaultAuthenticator = configurator.getVaultAuthenticator();

        StepVerifier.create(vaultAuthenticator.login())
                .expectSubscription()
                .expectErrorMatches(throwable -> {
                    assertEquals(throwable.getMessage(), "Could not perform login with vault. " +
                            "Please check your configuration");
                    return true;
                })
                .verify();
    }

    @SneakyThrows
    @Test
    void testHandleFailedAuth() {
        MockWebServer server = new MockWebServer();

        MockResponse response = new MockResponse()
                .addHeader("Content-Type", "application/json")
                .setResponseCode(400)
                .setBody("{\n" +
                        "  \"errors\": [\n" +
                        "    \"invalid role or secret ID\"\n" +
                        "  ]\n" +
                        "}");
        server.enqueue(response);
        server.start();


        VaultSecretsManagerProperties properties = VaultSecretsManagerProperties.builder()
                .token(null)
                .host("localhost")
                .port(server.getPort())
                .roleId("xxxx")
                .secretId("yyyy")
                .build();

        VaultSecretManagerConfigurator configurator = VaultSecretManagerConfigurator.builder()
                .withProperties(properties)
                .build();

        VaultAuthenticator vaultAuthenticator = configurator.getVaultAuthenticator();

        StepVerifier.create(vaultAuthenticator.login())
                .expectSubscription()
                .expectErrorMatches(throwable -> {
                    assertTrue(throwable.getMessage().contains("invalid role or secret ID"));
                    return true;
                })
                .verify();

        assertEquals("/v1/auth/approle/login", server.takeRequest().getPath());
        server.shutdown();
    }

    private String buildSuccessAuthResponse() {
        return "{\n" +
                "    \"request_id\": \"260fa017-e8e1-e3b5-a194-5ebe86e53275\",\n" +
                "    \"lease_id\": \"\",\n" +
                "    \"renewable\": false,\n" +
                "    \"lease_duration\": 0,\n" +
                "    \"data\": null,\n" +
                "    \"wrap_info\": null,\n" +
                "    \"warnings\": null,\n" +
                "    \"auth\": {\n" +
                "        \"client_token\": \"hvs.dummytoken\",\n" +
                "        \"accessor\": \"accessor.dummy\",\n" +
                "        \"policies\": [\n" +
                "            \"default\"\n" +
                "        ],\n" +
                "        \"token_policies\": [\n" +
                "            \"default\"\n" +
                "        ],\n" +
                "        \"metadata\": {\n" +
                "            \"role_name\": \"my-role\"\n" +
                "        },\n" +
                "        \"lease_duration\": 600,\n" +
                "        \"renewable\": true,\n" +
                "        \"entity_id\": \"656855e4-82b7-b874-6da7-ce2dff19711e\",\n" +
                "        \"token_type\": \"service\",\n" +
                "        \"orphan\": true,\n" +
                "        \"mfa_requirement\": null,\n" +
                "        \"num_uses\": 0\n" +
                "    }\n" +
                "}";
    }
}
