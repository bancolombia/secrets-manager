package co.com.bancolombia.secretsmanager.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AWSSecretsManagerConfigTest {

    private static AWSSecretsManagerConfig awsSecretsManagerConfig;

    @BeforeAll
    public static void setUp() {
        awsSecretsManagerConfig = AWSSecretsManagerConfig.builder().build();
    }

    @Test
    void validateAWSSecretsManagerConfig() {
        assertEquals(Region.US_EAST_1, awsSecretsManagerConfig.getRegion());
        assertEquals("", awsSecretsManagerConfig.getEndpoint());
        assertEquals(0, awsSecretsManagerConfig.getCacheSeconds());
        assertEquals(0, awsSecretsManagerConfig.getCacheSize());
    }

}
