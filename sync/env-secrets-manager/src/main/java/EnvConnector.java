import api.GenericManager;
import exceptions.SecretException;

/**
 * Represents an Environment Connector. It lets you to get secrets in
 * environment variables.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez
 * P.</a>
 */
public class EnvConnector implements GenericManager {

    @Override
    public String getSecret(String secretName) throws SecretException {
        String myEnv = System.getenv(secretName);
        if (myEnv == null) {
            throw new SecretException("The requested secret " + secretName + " was not found");
        } else {
            return myEnv;
        }
    }

    @Override
    public <T> T getSecret(String secretName, Class<T> cls){
        throw new UnsupportedOperationException("Serialization doesn't apply for env connector");
    }
}
