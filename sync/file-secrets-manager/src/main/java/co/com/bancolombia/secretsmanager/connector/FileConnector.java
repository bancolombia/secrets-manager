package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.GenericManager;
import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Represents an File Connector. It lets you to get secrets in files of the system.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez P.</a>
 */
@Getter
public class FileConnector implements GenericManager {

    public static final String PATH_DOCKER_LINUX = "/run/secrets/";  // NOSONAR
    public static final String PATH_DOCKER_WINDOWS = "C:\\ProgramData\\Docker\\secrets";  // NOSONAR

    private String path;

    public FileConnector(String path) {
        setPath(path);
    }

    /**
     * It sets path of secrets directory.
     *
     * @param path Secrets Directory
     */
    public void setPath(String path) {
        String lastValue = path.substring(path.length() - 1);
        String comparator = "";
        if (path.contains("/")) {
            comparator = "/";
        } else if (path.contains("\\")) {
            comparator = "\\";
        }
        if (!lastValue.equals(comparator)) {
            path += comparator;
        }

        this.path = path;
    }

    @Override
    public String getSecret(String secretName) throws SecretException {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(getPath() + secretName));
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SecretException(e.getMessage());
        }
    }


    @Override
    public <T> T getSecret(String secretName, Class<T> cls){
        throw new UnsupportedOperationException("Serialization doesn't apply for env connector");
    }

}
