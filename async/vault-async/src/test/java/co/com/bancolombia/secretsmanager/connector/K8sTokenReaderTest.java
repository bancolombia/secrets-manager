package co.com.bancolombia.secretsmanager.connector;

import lombok.SneakyThrows;
import org.junit.Test;
import reactor.test.StepVerifier;

public class K8sTokenReaderTest {

    @SneakyThrows
    @Test
    public void testReadToken() {
        StepVerifier.create(new K8sTokenReader().getKubernetesServiceAccountToken())
                .expectError().verify();
    }

    @SneakyThrows
    @Test
    public void testReadTokenWithPath() {
        StepVerifier.create(new K8sTokenReader("/tmp/file").getKubernetesServiceAccountToken())
                .expectError().verify();
    }


}
