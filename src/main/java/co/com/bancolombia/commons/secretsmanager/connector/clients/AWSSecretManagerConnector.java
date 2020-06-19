package co.com.bancolombia.commons.secretsmanager.connector.clients;

import co.com.bancolombia.commons.secretsmanager.connector.AbstractConnector;
import co.com.bancolombia.commons.secretsmanager.exceptions.SecretException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

/**
 * Represents an AWS Connector. It lets you to get Secrets of AWS Secrets
 * Manager Service.
 *
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez
 *         P.</a>
 */
public class AWSSecretManagerConnector extends AbstractConnector {

	private Region region;

	public AWSSecretManagerConnector(String region) {
		setRegion(region);
	}

	private void setRegion(String region) {
		this.region = Region.of(region);
	}

	@Override
	public String getSecret(String secretName) throws SecretException {
		SecretsManagerClient client = SecretsManagerClient.builder().region(region).build();

		String secret;
		GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder().secretId(secretName).build();
		GetSecretValueResponse getSecretValueResult = null;

		getSecretValueResult = client.getSecretValue(getSecretValueRequest);

		if (getSecretValueResult == null) {
			throw new SecretException("Secret value is null");
		} else {
			if (getSecretValueResult.secretString() != null) {
				secret = getSecretValueResult.secretString();
				return secret;
			}
			throw new SecretException("Secret value is not a String");
		}
	}
}
