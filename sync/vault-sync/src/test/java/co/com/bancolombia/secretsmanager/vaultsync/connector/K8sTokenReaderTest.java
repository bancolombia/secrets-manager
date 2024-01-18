package co.com.bancolombia.secretsmanager.vaultsync.connector;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.vaultsync.connector.K8sTokenReader;
import lombok.SneakyThrows;
import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class K8sTokenReaderTest {

    @SneakyThrows
    @Test
    public void testReadToken() {
        assertThrows(SecretException.class, () -> {
            new K8sTokenReader().getKubernetesServiceAccountToken();
        });
    }

    @SneakyThrows
    @Test
    public void testReadTokenWithPath() {
        assertThrows(SecretException.class, () -> {
            new K8sTokenReader("/tmp/file").getKubernetesServiceAccountToken();
        });
    }


}
