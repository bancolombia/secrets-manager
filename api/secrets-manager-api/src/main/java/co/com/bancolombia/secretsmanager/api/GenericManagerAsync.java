package co.com.bancolombia.secretsmanager.api;

import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import reactor.core.publisher.Mono;

public interface GenericManagerAsync {
    /**
     * Retrieves the value of a secret.
     *
     * @param secretName the name or identifier of the secret
     * @return a {@link Mono} emitting the secret value
     * @throws SecretException if the secret cannot be retrieved; implementations may instead
     *                         signal it as an error of the returned {@link Mono}
     */
    Mono<String> getSecret(String secretName) throws SecretException;

    /**
     * Retrieves a secret and deserializes its JSON value into the given type.
     *
     * @param secretName the name or identifier of the secret
     * @param cls        the target type
     * @param <T>        the target type
     * @return a {@link Mono} emitting the deserialized secret
     * @throws SecretException if the secret cannot be retrieved or deserialized; implementations
     *                         may instead signal it as an error of the returned {@link Mono}
     */
    <T> Mono<T> getSecret(String secretName, Class<T> cls) throws SecretException;
}
