package co.com.bancolombia.secretsmanager.config;

import org.junit.BeforeClass;
import org.junit.Test;
import software.amazon.awssdk.regions.Region;

import static org.junit.Assert.assertEquals;

public class AWSSecretsManagerConfigTest {

    private static AWSSecretsManagerConfig awsSecretsManagerConfig;

    @BeforeClass
    public static void setUp() {
        awsSecretsManagerConfig = AWSSecretsManagerConfig.builder().build();
    }

    @Test
    public void validateAWSSecretsManagerConfig() {
        assertEquals(Region.US_EAST_1, awsSecretsManagerConfig.getRegion());
        assertEquals("", awsSecretsManagerConfig.getEndpoint());
        assertEquals(0, awsSecretsManagerConfig.getCacheSeconds());
        assertEquals(0, awsSecretsManagerConfig.getCacheSize());
    }

}
