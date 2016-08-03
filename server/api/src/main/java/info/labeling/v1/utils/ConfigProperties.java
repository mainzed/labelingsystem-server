package info.labeling.v1.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProperties {
    
    private static final Properties prop = new Properties();
	private static final String fileName = "config.properties";
	private static final String HOST = "host";
	private static final String SESAMESERVER = "ts_server";
	private static final String REPOSITORY = "repository";
	private static final String LSDETAILHTML = "ls_detailhtml";
    
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

	public static String getFileName() {
		return fileName;
	}

	public static String getHOST() {
		return HOST;
	}

	public static String getSESAMESERVER() {
		return SESAMESERVER;
	}

	public static String getREPOSITORY() {
		return REPOSITORY;
	}

	public static String getLSDETAIL() {
		return LSDETAILHTML;
	}
    
}
