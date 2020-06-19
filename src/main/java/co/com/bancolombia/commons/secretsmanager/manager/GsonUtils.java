package co.com.bancolombia.commons.secretsmanager.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Singleton - Gson
 *
 * @author jhagutie@bancolombia.com.co IAS
 * @version 1.0.0
 * @since JDK 1.8
 */
public class GsonUtils {
	
	private final Gson gson;
	
	private GsonUtils() {
		gson = new GsonBuilder()
				.create();
	}
    
    public static GsonUtils getInstance() {
        return GsonUtilsHolder.INSTANCE;
    }
    
    private static class GsonUtilsHolder {
        private static final GsonUtils INSTANCE = new GsonUtils();
    }
    
    /**
     * JSON String to Model or Entity
     * 
     * @param data JSON string
     * @param model Entity to convert
     * @return T object
     */
    public <T> T stringToModel(String data, Class<T> model) {
    	return gson.fromJson(data, model);
    }

}
