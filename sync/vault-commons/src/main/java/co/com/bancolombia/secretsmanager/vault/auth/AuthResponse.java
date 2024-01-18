package co.com.bancolombia.secretsmanager.vault.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String clientToken;
    private String accessor;
    @Builder.Default
    private List<String> policies = new ArrayList<>();
    @Builder.Default
    private List<String> tokenPolicies = new ArrayList<>();
    private Map<String, String> metadata;
    private long leaseDuration;
    private boolean renewable;
}
