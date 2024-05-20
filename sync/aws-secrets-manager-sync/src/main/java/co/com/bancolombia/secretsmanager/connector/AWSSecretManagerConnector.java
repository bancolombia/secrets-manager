package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.GenericManager;
import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.commons.utils.GsonUtils;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.net.URI;
import java.util.Optional;

public class AWSSecretManagerConnector implements GenericManager {

    private final SecretsManagerClient client;

    public AWSSecretManagerConnector(String region) {
        this.client = buildClient(SecretsManagerClient.builder(), region, Optional.empty());
    }

    /**
     * This constructor allows make a connection for a local instance of
     * AWS Secrets Manager, such as: LocalStack, Docker container, etc.
     *
     * @param endpoint : String uri connection
     * @param region   : Dummy region for Amazon SDK Client
     */
    public AWSSecretManagerConnector(String region, String endpoint) {
        this.client = buildClient(SecretsManagerClient.builder(), region, Optional.of(URI.create(endpoint)));
    }

    /**
     * for testing
     *
     * @param region
     * @param builder
     */
    public AWSSecretManagerConnector(String region, SecretsManagerClientBuilder builder) {
        this.client = buildClient(builder, region, Optional.empty());
    }

    @Override
    public String getSecret(String secretName) throws SecretException {
        return getSecretInternal(secretName);
    }

    @Override
    public <T> T getSecret(String secretName, Class<T> cls) throws SecretException {
        String data = this.getSecret(secretName);
        try {
            return GsonUtils.getInstance().stringToModel(data, cls);
        } catch (Exception e) {
            throw new SecretException(e.getMessage());
        }
    }

    private String getSecretInternal(String secretName) throws SecretException {
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();
        GetSecretValueResponse getSecretValueResult;

        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (Exception e) {
            throw new SecretException(e.getMessage());
        }

        if (getSecretValueResult == null) {
            throw new SecretException("Secret value is null");
        } else {
            if (getSecretValueResult.secretString() != null) {
                return getSecretValueResult.secretString();
            }
            throw new SecretException("Secret value is not a String");
        }
    }

    /**
     * Default provider chain extended with extra CredentialProvider and
     * specif order defined.
     *
     * @see AwsCredentialsProviderChain
     */
    private AwsCredentialsProviderChain getProviderChain() {
        return AwsCredentialsProviderChain.builder()
                .addCredentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .addCredentialsProvider(SystemPropertyCredentialsProvider.create())
                .addCredentialsProvider(WebIdentityTokenFileCredentialsProvider.create())
                .addCredentialsProvider(ProfileCredentialsProvider.create())
                .addCredentialsProvider(ContainerCredentialsProvider.builder().build())
                .addCredentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();
    }

    private SecretsManagerClient buildClient(SecretsManagerClientBuilder builder, String region, Optional<URI> endpoint) {
        SecretsManagerClient.builder()
                .credentialsProvider(getProviderChain())
                .region(Region.of(region));
        endpoint.ifPresent(builder::endpointOverride);
        return builder.build();
    }

}
