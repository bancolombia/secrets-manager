package co.com.bancolombia.secretsmanager.connector;

import co.com.bancolombia.secretsmanager.api.GenericManager;
import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.SsmClientBuilder;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

import java.net.URI;
import java.util.Optional;

public class AWSParameterStoreConnector implements GenericManager {
    private final SsmClient client;

    public AWSParameterStoreConnector(String region) {
        this.client = buildClient(SsmClient.builder(), region, Optional.empty());
    }

    /**
     * This constructor allows make a connection for a local instance of
     * AWS Parameter Store, such as: LocalStack, Docker container, etc.
     *
     * @param endpoint : String uri connection
     * @param region   : Dummy region for Amazon SDK Client
     */
    public AWSParameterStoreConnector(String region, String endpoint) {
        this.client = buildClient(SsmClient.builder(), region, Optional.of(URI.create(endpoint)));
    }

    /**
     * for testing
     *
     * @param region
     * @param builder
     */
    public AWSParameterStoreConnector(String region, SsmClientBuilder builder) {
        this.client = buildClient(builder, region, Optional.empty());
    }

    @Override
    public String getSecret(String secretName) throws SecretException {
        return getSecretInternal(secretName);
    }

    @Override
    public <T> T getSecret(String secretName, Class<T> cls) {
        throw new UnsupportedOperationException("Serialization doesn't apply for parameter store connector");
    }

    private String getSecretInternal(String secretName) throws SecretException {
        GetParameterRequest getParameterRequest = GetParameterRequest.builder().name(secretName).build();
        GetParameterResponse getParameterResponse;

        try {
            getParameterResponse = client.getParameter(getParameterRequest);
        } catch (Exception e) {
            throw new SecretException(e.getMessage());
        }

        if (getParameterResponse == null) {
            throw new SecretException("Secret value is null");
        } else {
            if (getParameterResponse.parameter().value() != null) {
                return getParameterResponse.parameter().value();
            }
            throw new SecretException("Secret value is not a String");
        }
    }

    private SsmClient buildClient(SsmClientBuilder builder, String region, Optional<URI> endpoint) {
        builder.credentialsProvider(getProviderChain());
        builder.region(Region.of(region));
        endpoint.ifPresent(builder::endpointOverride);
        return builder.build();
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

}
