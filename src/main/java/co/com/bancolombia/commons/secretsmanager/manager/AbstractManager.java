package co.com.bancolombia.commons.secretsmanager.manager;

import co.com.bancolombia.commons.secretsmanager.connector.AbstractConnector;
import co.com.bancolombia.commons.secretsmanager.exceptions.SecretException;

/**
 * Represents an abstract class of a Manager.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez
 * P.</a>
 */
public abstract class AbstractManager {

    protected AbstractConnector connector;

    public AbstractManager(AbstractConnector connector) {
        this.connector = connector;
    }

    public abstract String getSecret(String secretName) throws SecretException;

}
