package co.com.bancolombia.secretsmanager.vault.ssl;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.vault.K8sTokenReader;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.nio.file.NoSuchFileException;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
        SecretException ex = assertThrows(SecretException.class, () ->
                new K8sTokenReader("/path/does/not/exist/token").getKubernetesServiceAccountToken());
        assertInstanceOf(NoSuchFileException.class, ex.getCause());
    }


}
