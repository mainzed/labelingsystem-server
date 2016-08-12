package info.labeling.v1.utils;

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
	private static final String fileName = "config.properties";

	/**
	 * load property file
	 *
	 * @param fileName
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
	 * @param param
	 * @return param value
	 * @throws IOException
	 */
	public static String getPropertyParam(String param) throws IOException {
		loadpropertyFile(fileName);
		return prop.getProperty(param);
	}

}
