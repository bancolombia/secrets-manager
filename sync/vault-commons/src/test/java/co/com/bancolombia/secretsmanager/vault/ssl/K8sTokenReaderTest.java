package co.com.bancolombia.secretsmanager.vault.ssl;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.vault.K8sTokenReader;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class K8sTokenReaderTest {

    @SneakyThrows
    @Test
    void testReadToken() {
        assertThrows(SecretException.class, () -> {
            new K8sTokenReader().getKubernetesServiceAccountToken();
        });
    }

    @SneakyThrows
    @Test
    void testReadTokenWithPath() {
        assertThrows(SecretException.class, () -> {
            new K8sTokenReader("/tmp/file").getKubernetesServiceAccountToken();
        });
    }


}
