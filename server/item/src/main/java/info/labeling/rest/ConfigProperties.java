package info.labeling.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProperties {

    private static final Properties prop = new Properties();
    private static final String fileName = "config.properties";

    public static boolean loadpropertyFile(String fileName) throws IOException {
        InputStream input = null;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            input = classLoader.getResourceAsStream(fileName);
            prop.load(input);
            return true;
        } catch (Exception e) {
            throw new IOException(e.toString());
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    public static String getPropertyParam(String param) throws IOException {
        loadpropertyFile(fileName);
        return prop.getProperty(param);
    }

}
