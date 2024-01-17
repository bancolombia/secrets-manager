package co.com.bancolombia.secretsmanager.vaultsync.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CacheProperties {

    @Builder.Default
    private int expireAfter = 600; //in seconds

    @Builder.Default
    private int maxSize = 100;
}
