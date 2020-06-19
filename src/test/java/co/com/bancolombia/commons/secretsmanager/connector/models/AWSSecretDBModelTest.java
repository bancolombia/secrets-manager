package co.com.bancolombia.commons.secretsmanager.connector.models;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Represents an AWS Secret DB Model. It lets you to test a AWS Secret DB Model.
 * 
 * @author <a href="mailto:andmagom@bancolombia.com.co">Andrés Mauricio Gómez
 *         P.</a>
 */
public final class AWSSecretDBModelTest {

	@Test
	public void conversionOk() {
		AWSSecretDBModel model = AWSSecretDBModel
				.getModel("{\"username\":\"root\"," + "\"password\":\"123456789\",\"engine\":\"oracle\","
						+ "\"host\":\"jdbc:oracle:thin:@oauth-oracle.cufapur4ayuj"
						+ ".us-east-1.rds.amazonaws.com:1521:ORCL\"," + "\"port\":\"3306\",\"dbname\":\"ROOT\"}");
		assertNotNull(model);
		assertEquals("root", model.getUsername());
		assertEquals("123456789", model.getPassword());
		assertEquals("oracle", model.getEngine());
		assertEquals("jdbc:oracle:thin:@oauth-oracle.cufapur4ayuj.us-east-1.rds.amazonaws.com:1521:ORCL", model.getHost());
		assertEquals("3306", model.getPort());
		assertEquals("ROOT", model.getDbname());
	}

	@Test(expected = Exception.class)
	public void conversionFail() {
		AWSSecretDBModel.getModel("{\"username\"\"root\",\"passwords\":\"123456789\",\"engine\":\"oracle\"}");
	}

}
