package co.com.bancolombia.secretsmanager.config;

import org.junit.BeforeClass;
import org.junit.Test;
import software.amazon.awssdk.regions.Region;

import static org.junit.Assert.assertEquals;

public class AWSParameterStoreConfigTest {

    private static AWSParameterStoreConfig awsParameterStoreConfig;

    @BeforeClass
    public static void setUp() {
        awsParameterStoreConfig = AWSParameterStoreConfig.builder().build();
    }

    @Test
    public void validateAWSParameterStoreConfig() {
        assertEquals(Region.US_EAST_1, awsParameterStoreConfig.getRegion());
        assertEquals("", awsParameterStoreConfig.getEndpoint());
        assertEquals(0, awsParameterStoreConfig.getCacheSeconds());
        assertEquals(0, awsParameterStoreConfig.getCacheSize());
    }

}
