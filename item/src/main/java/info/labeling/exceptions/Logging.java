package info.labeling.exceptions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.jamesmurty.utils.XMLBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * CLASS for show errormessages
 *
 * @author Florian Thiery M.Sc.
 */
public class Logging {

	/**
	 * XML ERROR MESSAGE for catch exceptions
	 *
	 * @param exception this exception
	 * @param endClass class path where exception tree ends
	 * @return error message XML
	 * @throws javax.xml.parsers.ParserConfigurationException
	 * @throws javax.xml.transform.TransformerException
	 */
	public static String getMessageXML(Exception exception, String endClass) throws ParserConfigurationException, TransformerException {
		XMLBuilder xml = XMLBuilder.create("error")
				.e("message")
				.t(exception.toString())
				.up();
		for (StackTraceElement element : exception.getStackTrace()) {
			xml.e("description")
					.t(element.getClassName() + " / " + element.getMethodName() + "() / " + element.getLineNumber())
					.up();
			if (element.getClassName().equals(endClass)) {
				break;
			}
		}
		return xml.asString();
	}

	/**
	 * JSON ERROR MESSAGE for catch exceptions
	 *
	 * @param exception this exception
	 * @param endClass class path where exception tree ends
	 * @return error message JSON
	 */
	public static String getMessageJSON(Exception exception, String endClass) {
		// START BUILD JSON
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JSONObject jsonobj_error = new JSONObject(); // {}
		JSONObject jsonobj_error_data = new JSONObject(); // {}
		JSONArray jsonarray_element = new JSONArray();
		for (StackTraceElement element : exception.getStackTrace()) {
			jsonarray_element.add(element.getClassName() + " / " + element.getMethodName() + "() / " + element.getLineNumber());
			if (element.getClassName().equals(endClass)) {
				break;
			}
		}
		jsonobj_error.put("error", jsonobj_error_data);
		jsonobj_error_data.put("message", exception.toString());
		jsonobj_error_data.put("description", jsonarray_element);
		// OUTPUT AS pretty print JSON 
		return gson.toJson(jsonobj_error);
	}

	/**
	 * TEXT ERROR MESSAGE for catch exceptions
	 *
	 * @param exception this exception
	 * @param endClass class path where exception tree ends
	 * @return error message TEXT
	 */
	public static String getMessageTEXT(Exception exception, String endClass) {
		String message = "error\n";
		message += "message: \"" + exception.toString() + "\"";
		for (StackTraceElement element : exception.getStackTrace()) {
			message += "\ndescription: \"" + element.getClassName() + " / " + element.getMethodName() + "() / " + element.getLineNumber() + "\"";
			if (element.getClassName().equals(endClass)) {
				break;
			}
		}
		return message;
	}

}
