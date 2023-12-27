package co.com.bancolombia.secretsmanager.config;

import lombok.*;

import java.io.File;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class VaultSecretsManagerProperties {
    private String key;

    @Builder.Default
    private String host = "localhost";

    @Builder.Default
    private int port = 8200;

    @Builder.Default
    private boolean ssl = false;

    @Builder.Default
    private boolean sslVerify = false;

    @Builder.Default
    private String baseApi = "/v1";

    @Builder.Default
    private String baseSecrets = "/kv/data/";

    private String token;

    private String roleId;

    private String secretId;

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

    public String buildUrl() {
        return String.format("%s://%s:%d%s", ssl ? "https" : "http", host, port, baseApi);
    }

    public boolean roleCredentialsProvided() {
        return roleId != null && !roleId.isEmpty() && secretId != null && !secretId.isEmpty();
    }
}
