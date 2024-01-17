package co.com.bancolombia.secretsmanager.connector.auth;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class RoleAuth {
    @SerializedName("role_id")
    private String roleId;

    @SerializedName("secret_id")
    private String secretId;
}
