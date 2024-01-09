package co.com.bancolombia.secretsmanager.connector.auth;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class K8sTokenReader {

    private final String tokenFile;

    public K8sTokenReader() {
        this.tokenFile = "/var/run/secrets/kubernetes.io/serviceaccount/token";
    }

    public K8sTokenReader(String tokenFile) {
        this.tokenFile = tokenFile;
    }

    /**
     * Reads the kubernetes service account token from the file system
     * @return the kubernetes service account token
     */
    public Mono<String> getKubernetesServiceAccountToken() {
        return Mono.fromCallable(() -> {
                    try {
                        Path path = Paths.get(this.tokenFile);
                        return Files.readAllLines(path).get(0);
                    } catch (Exception e) {
                        throw new SecretException("Error reading kubernetes service account token: " + e.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

}
