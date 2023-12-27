package co.com.bancolombia.secretsmanager.connector.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
    private long leaseDuration;
    private boolean renewable;
}
