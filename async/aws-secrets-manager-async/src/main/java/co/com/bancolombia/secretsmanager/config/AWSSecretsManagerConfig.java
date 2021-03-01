package co.com.bancolombia.secretsmanager.config;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.util.Optional;

@Builder
@Getter
public class AWSSecretsManagerConfig {
    @Builder.Default
    private Region region = Region.US_EAST_1;
    @Builder.Default
    private String endpoint="";
    @Builder.Default
    private int cacheSeconds=0;
    @Builder.Default
    private int cacheSize=0;
}
