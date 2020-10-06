import lombok.Data;

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