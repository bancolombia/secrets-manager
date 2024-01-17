package co.com.bancolombia.secretsmanager.vaultsync.connector.secret;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SecretPayload {
    private Map<String, Object> data;
    private Map<String, Object> metadata;
}
