package co.com.bancolombia.commons.secretsmanager.connector.clients;

import co.com.bancolombia.commons.secretsmanager.connector.AbstractConnector;
import co.com.bancolombia.commons.secretsmanager.exceptions.SecretException;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.net.URI;
import java.util.Optional;

/**
 * Represents an AWS Connector. It lets you to get Secrets of AWS Secrets
 * Manager Service.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez
 * P.</a>
 */
public class AWSSecretManagerConnector extends AbstractConnector {

    private Region region;
    private Optional<URI> endpoint;

    public AWSSecretManagerConnector(String region) {
        setRegion(region);
        endpoint = Optional.empty();
    }

    /**
     * This constructor allows make a connection for a local instance of
     * AWS Secrets Manager, such as: LocalStack, Docker container, etc.
     *
     * @param endpoint : String uri connection
     * @param region   : Dummy region for Amazon SDK Client
     */
    public AWSSecretManagerConnector(String region, String endpoint) {
        this.endpoint = Optional.of(URI.create(endpoint));
        this.region = Region.of(region);
    }

    private void setRegion(String region) {
        this.region = Region.of(region);
    }

    @Override
    public String getSecret(String secretName) throws SecretException {
        SecretsManagerClient client = buildClient();
        return getSecret(secretName, client);
    }

    private String getSecret(String secretName, SecretsManagerClient client) throws SecretException {
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();
        GetSecretValueResponse getSecretValueResult = null;

        getSecretValueResult = client.getSecretValue(getSecretValueRequest);

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

    private SecretsManagerClient buildClient() {
        SecretsManagerClientBuilder clientBuilder = SecretsManagerClient.builder()
                .credentialsProvider(getProviderChain())
                .region(region);

        endpoint.ifPresent(clientBuilder::endpointOverride);
        return clientBuilder.build();
    }

}
