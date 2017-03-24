package info.labeling.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * CLASS for property file actions
 *
 * @author Florian Thiery M.Sc.
 */
public class ConfigProperties {

	private static final Properties prop = new Properties();
	private static final String fileName = "configDevServer.properties";

	/**
	 * load property file
	 *
	 * @param fileName name of the properties file
	 * @return loaded or not
	 * @throws IOException
	 */
	private static boolean loadpropertyFile(String fileName) throws IOException {
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

	/**
	 * get parameter of property file
	 *
	 * @param param param name in the properties file
	 * @return param value
	 * @throws IOException
	 */
	public static String getPropertyParam(String param) throws IOException {
		loadpropertyFile(fileName);
		return prop.getProperty(param);
	}

}
