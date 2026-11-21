package co.com.bancolombia.secretsmanager.api;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;

public interface GenericManager {
    /**
     * Retrieves the value of a secret.
     *
     * @param secretName the name or identifier of the secret
     * @return the secret value
     * @throws SecretException if the secret cannot be retrieved or its value is missing
     */
    String getSecret(String secretName) throws SecretException;

    /**
     * Retrieves a secret and deserializes its JSON value into the given type.
     *
     * @param secretName the name or identifier of the secret
     * @param cls        the target type
     * @param <T>        the target type
     * @return the deserialized secret
     * @throws SecretException if the secret cannot be retrieved or deserialized into {@code cls}
     */
    <T> T getSecret(String secretName, Class<T> cls) throws SecretException;
}
