package co.com.bancolombia.commons.secretsmanager.connector;

import co.com.bancolombia.commons.secretsmanager.exceptions.SecretException;

/**
 * Represents an abstract class of a Connector.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez P</a>
 */
public abstract class AbstractConnector {

    public abstract String getSecret(String secretName) throws SecretException;

}
