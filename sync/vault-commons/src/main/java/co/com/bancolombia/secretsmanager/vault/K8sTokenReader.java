package co.com.bancolombia.secretsmanager.vault;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class K8sTokenReader {

    private final String tokenFile;

    public K8sTokenReader() {
        this.tokenFile = "/var/run/secrets/kubernetes.io/serviceaccount/token";
    }

    public K8sTokenReader(String tokenFile) {
        this.tokenFile = tokenFile;
    }

    /**
     * Reads the Kubernetes service account token from the file system
     *
     * @return the Kubernetes service account token
     * @throws SecretException if the token file cannot be read or is empty
     */
    public String getKubernetesServiceAccountToken() throws SecretException {
        try {
            Path path = Paths.get(this.tokenFile);
            return Files.readAllLines(path).get(0);
        } catch (Exception e) {
            throw new SecretException("Error reading kubernetes service account token: " +
                    Objects.toString(e.getMessage(), e.toString()), e
            );
        }
    }

}
