package co.com.bancolombia.secretsmanager.commons.utils;

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
     * @param data  JSON string
     * @param model Entity to convert
     * @param <T>   object
     * @return T object
     */
    public <T> T stringToModel(String data, Class<T> model) {
        return gson.fromJson(data, model);
    }

    public String modelToString(Object model) {
        return gson.toJson(model);
    }

}
