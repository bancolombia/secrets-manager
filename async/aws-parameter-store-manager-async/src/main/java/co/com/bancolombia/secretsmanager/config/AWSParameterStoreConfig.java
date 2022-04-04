package co.com.bancolombia.secretsmanager.config;

import lombok.Builder;
import lombok.Getter;
import software.amazon.awssdk.regions.Region;

@Builder
@Getter
public class AWSParameterStoreConfig {
    @Builder.Default
    private Region region = Region.US_EAST_1;
    @Builder.Default
    private String endpoint="";
    @Builder.Default
    private int cacheSeconds=0;
    @Builder.Default
    private int cacheSize=0;
}
