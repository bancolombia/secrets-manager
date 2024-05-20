package co.com.bancolombia.secretsmanager.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AWSParameterStoreConfigTest {

    private static AWSParameterStoreConfig awsParameterStoreConfig;

    @BeforeAll
    public static void setUp() {
        awsParameterStoreConfig = AWSParameterStoreConfig.builder().build();
    }

    @Test
    void validateAWSParameterStoreConfig() {
        assertEquals(Region.US_EAST_1, awsParameterStoreConfig.getRegion());
        assertEquals("", awsParameterStoreConfig.getEndpoint());
        assertEquals(0, awsParameterStoreConfig.getCacheSeconds());
        assertEquals(0, awsParameterStoreConfig.getCacheSize());
    }

}
