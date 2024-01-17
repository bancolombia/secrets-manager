package co.com.bancolombia.secretsmanager.config;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class VaultSecretsManagerProperties {

    @Builder.Default
    private String host = "localhost";

    @Builder.Default
    private int port = 8200;

    @Builder.Default
    private boolean ssl = false;

    @Builder.Default
    private boolean tlsAuth = false;

    @Builder.Default
    private String baseApi = "/v1";

    @Builder.Default
    private String baseSecrets = "/kv/data/";

    private String token;

    private String roleId;

    private String secretId;

    private String vaultRoleForK8sAuth;

    @Builder.Default
    private int engineVersion = 2;

    private VaultTrustStoreProperties trustStoreProperties;

    private VaultKeyStoreProperties keyStoreProperties;

    @Builder.Default
    private HttpProperties httpProperties = HttpProperties.builder().connectionTimeout(5).build();

    @Builder.Default
    private CacheProperties authCacheProperties = CacheProperties.builder().expireAfter(600).maxSize(10).build();

    @Builder.Default
    private CacheProperties secretsCacheProperties= CacheProperties.builder().expireAfter(600).maxSize(100).build();

    @Builder.Default
    private String appRoleAuthPath = "/auth/approle/login";

    @Builder.Default
    private String k8sAuthPath = "/auth/kubernetes/login";

    public String buildUrl() {
        return String.format("%s://%s:%d%s", ssl ? "https" : "http", host, port, baseApi);
    }

    public boolean isRoleCredentialsProvided() {
        return roleId != null && !roleId.isEmpty() && secretId != null && !secretId.isEmpty();
    }

    public boolean isRoleNameForK8sProvided() {
        return vaultRoleForK8sAuth != null && !vaultRoleForK8sAuth.isEmpty();
    }

    public boolean isTokenProvided() {
        return token != null && !token.isEmpty();
    }
}
