package co.com.bancolombia.parameterstore.connector;

import co.com.bancolombia.secretsmanager.api.GenericManager;
import co.com.bancolombia.secretsmanager.api.exceptions.SecretException;
import co.com.bancolombia.secretsmanager.commons.utils.AWSUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.SsmClientBuilder;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;

import java.net.URI;
import java.util.Optional;

public class AWSParameterStoreConnector implements GenericManager {

    private Region region;
    private Optional<URI> endpoint;

    public AWSParameterStoreConnector(String region) {
        AWSUtils.setRegion(region);
        endpoint = Optional.empty();
    }

    /**
     * This constructor allows make a connection for a local instance of
     * AWS Secrets Manager, such as: LocalStack, Docker container, etc.
     *
     * @param endpoint : String uri connection
     * @param region   : Dummy region for Amazon SDK Client
     */
    public AWSParameterStoreConnector(String region, String endpoint) {
        this.endpoint = Optional.of(URI.create(endpoint));
        this.region = Region.of(region);
    }

    @Override
    public String getSecret(String secretName) throws SecretException {
        SsmClient ssmClient = buildClient();
        return getSecret(secretName, ssmClient);

    }

    @Override
    public <T> T getSecret(String secretName, Class<T> cls) throws SecretException {
        throw new UnsupportedOperationException("Serialization doesn't apply for parameter store connector");
    }

    private String getSecret(String secretName, SsmClient client) throws SecretException {
        GetParameterRequest getParameterRequest = GetParameterRequest.builder().name(secretName).build();
        GetParameterResponse getParameterResponse = client.getParameter(getParameterRequest);
        if (getParameterResponse == null) {
            throw new SecretException("Secret value is null");
        } else {
            if (getParameterResponse.parameter().value() != null) {
                return getParameterResponse.parameter().value();
            }
            throw new SecretException("Secret value is not a String");
        }
    }

    private SsmClient buildClient() {
        SsmClientBuilder clientBuilder = SsmClient.builder()
                .credentialsProvider(AWSUtils.getProviderChain())
                .region(region);

        endpoint.ifPresent(clientBuilder::endpointOverride);
        return clientBuilder.build();
    }

}
