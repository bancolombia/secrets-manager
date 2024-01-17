package co.com.bancolombia.secretsmanager.connector.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class K8sAuth {
    private String jwt;
    private String role;
}
