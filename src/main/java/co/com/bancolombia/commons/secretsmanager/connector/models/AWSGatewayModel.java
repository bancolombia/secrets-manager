package co.com.bancolombia.commons.secretsmanager.connector.models;

import co.com.bancolombia.commons.secretsmanager.manager.GsonUtils;
import lombok.Data;

/**
 * Represents an ApiGateway Model. It lets you to convert a DB Secret of AWS
 * Secrets Manager Service to a object.
 * 
 * @author jhagutie@bancolombia.com.co
 */
@Data
public class AWSGatewayModel {
	
	private String id;
	private String apiKey;

	public static AWSGatewayModel getModel(String data) {
		return GsonUtils.getInstance().stringToModel(data, AWSGatewayModel.class);
	}
	
}
