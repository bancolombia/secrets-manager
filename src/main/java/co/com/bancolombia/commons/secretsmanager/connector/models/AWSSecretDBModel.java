package co.com.bancolombia.commons.secretsmanager.connector.models;

import co.com.bancolombia.commons.secretsmanager.manager.GsonUtils;
import lombok.Data;

/**
 * Represents an DB Secret Model. It lets you to convert a DB Secret of AWS
 * Secrets Manager Service to a object.
 * 
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez P.</a>
 */

@Data
public final class AWSSecretDBModel {

	private String username;
	private String password;
	private String host;
	private String port;
	private String dbname;
	private String engine;
	private String dbInstanceIdentifier;

	public static AWSSecretDBModel getModel(String data) {
		return GsonUtils.getInstance().stringToModel(data, AWSSecretDBModel.class);
	}
}
