import exceptions.SecretException;

public interface GenericManager {
    String getSecret(String secretName) throws SecretException;
    <T> T getSecret(String secretName, Class<T> cls) throws SecretException;
}
