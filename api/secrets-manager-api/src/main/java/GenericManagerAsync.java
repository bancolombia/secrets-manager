import exceptions.SecretException;

public interface GenericManagerAsync {
    String getSecret(String secretName) throws SecretException;
    <T> T getSecret(String secretName, Class<T> cls) throws SecretException;
}
