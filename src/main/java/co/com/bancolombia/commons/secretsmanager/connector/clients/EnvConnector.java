package co.com.bancolombia.commons.secretsmanager.connector.clients;

import co.com.bancolombia.commons.secretsmanager.connector.AbstractConnector;
import co.com.bancolombia.commons.secretsmanager.exceptions.SecretException;

/**
 * Represents an Environment Connector. It lets you to get secrets in
 * environment variables.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez
 * P.</a>
 */
public class EnvConnector extends AbstractConnector {

    @Override
    public String getSecret(String secretName) throws SecretException {
        String myEnv = System.getenv(secretName);
        if (myEnv == null) {
            throw new SecretException("The requested secret " + secretName + " was not found");
        } else {
            return myEnv;
        }
    }
}
