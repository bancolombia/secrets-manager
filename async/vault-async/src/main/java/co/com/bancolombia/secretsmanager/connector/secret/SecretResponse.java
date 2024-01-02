package co.com.bancolombia.secretsmanager.connector.secret;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SecretResponse {

    @SerializedName("request_id")
    private String requestId;

    @SerializedName("lease_id")
    private String leaseId;
    private boolean renewable;

    @SerializedName("lease_duration")
    private int leaseDuration;
    private SecretPayload data;
}
