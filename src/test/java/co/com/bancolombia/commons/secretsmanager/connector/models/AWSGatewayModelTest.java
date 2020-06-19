package co.com.bancolombia.commons.secretsmanager.connector.models;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AWSGatewayModelTest {

	@Test
	public void methodSets() {
		AWSGatewayModel model = new AWSGatewayModel();
		model.setId("id-api-gateway");
		model.setApiKey("api-key-api-gateway");
		
		assertNotNull(model);
		assertNotNull(model.getId());
		assertNotNull(model.getApiKey());
		assertEquals("id-api-gateway", model.getId());
		assertEquals("api-key-api-gateway", model.getApiKey());
	}
	
	@Test
	public void conversionOk() {
		AWSGatewayModel model = AWSGatewayModel
				.getModel("{\"id\":\"id-api-gateway\"," + "\"apiKey\":\"api-key-api-gateway\"}");
		assertNotNull(model);
		assertNotNull(model.getId());
		assertNotNull(model.getApiKey());
		assertEquals("id-api-gateway", model.getId());
		assertEquals("api-key-api-gateway", model.getApiKey());
	}

	@Test(expected = Exception.class)
	public void conversionFail() {
		AWSGatewayModel.getModel("{\"apiKey\"\"api-key-api-gateway\"}");
	}

}