package co.com.bancolombia.commons.secretsmanager.manager;

import co.com.bancolombia.commons.secretsmanager.connector.AbstractConnector;
import co.com.bancolombia.commons.secretsmanager.exceptions.SecretException;

/**
 * Represents an Generic Manager. It lets you to get secrets of any connector.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez P.</a>
 */
public class GenericManager extends AbstractManager {

    public GenericManager(AbstractConnector connector) {
        super(connector);
    }

    @Override
    public String getSecret(String secretName) throws SecretException {
        return super.connector.getSecret(secretName);
    }

    /**
     * It gets the secret in object format.
     *
     * @param secretName Secret Name
     * @param cls        Class type T to return
     * @param <T>        Class type T to return
     * @return Object T with data of the secret
     * @throws SecretException Exception Object if there is any error
     */
    public <T> T getSecretModel(String secretName, Class<T> cls) throws SecretException {
        String data = super.connector.getSecret(secretName);
        try {
            return GsonUtils.getInstance().stringToModel(data, cls);
        } catch (Exception e) {
            throw new SecretException(e.getMessage());
        }
    }
}
