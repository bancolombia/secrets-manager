package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.config.AWSSecretsManagerConfig;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import software.amazon.awssdk.regions.Region;

public class AWSSecretManagerConnectorAsyncTest {

    @Test
    public void buildClient() {
        AWSSecretsManagerConfig config = AWSSecretsManagerConfig.builder()
                .cacheSeconds(5)
                .cacheSize(10)
                .region(Region.US_EAST_1)
                .endpoint("http://localhost.com")
                .build();
        AWSSecretManagerConnectorAsync client = new AWSSecretManagerConnectorAsync(config);
        assertNotNull(client);
    }
}
