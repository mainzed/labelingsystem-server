package exceptions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.jamesmurty.utils.XMLBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * ERROR MESSAGE for catch exceptions
 *
 * @author Florian Thiery M.Sc.
 * @author i3mainz - Institute for Spatial Information and Surveying Technology
 * @version 18.02.2015
 */
public class Logging {

    /**
     * XML ERROR MESSAGE for catch exceptions
     *
     * @param exception
     * @param endClass
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
     * @param exception
     * @param endClass
     * @return error message JSON
     */
    public static String getMessageJSON(Exception exception, String endClass) {
        // START BUILD JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JSONObject jsonobj_error = new JSONObject(); // {}
        JSONObject jsonobj_error_data = new JSONObject(); // {}
        JSONArray jsonarray_element = new JSONArray();
        for (StackTraceElement element : exception.getStackTrace()) {
            JSONObject errMessage = new JSONObject();
            errMessage.put("class", element.getClassName());
            errMessage.put("method", element.getMethodName());
            errMessage.put("line", element.getLineNumber());
            jsonarray_element.add(errMessage);
            //jsonarray_element.add(element.getClassName() + " / " + element.getMethodName() + "() / " + element.getLineNumber());
            if (element.getClassName().equals(endClass)) {
                break;
            }
        }
        // get error code
        String code = "";
        String userMessage = "";
        if (exception.toString().contains("NullPointerException")) {
            code = "1";
            userMessage = "some value is not available";
        } else if (exception.toString().contains("ValidateJSONObjectException")) {
            code = "2";
			String[] ex = exception.toString().split(": ");
            userMessage = "validate JSON object exception: " + ex[1];
        }
        // output
        jsonobj_error.put("errors", jsonobj_error_data);
        jsonobj_error_data.put("internalMessage", exception.toString());
        jsonobj_error_data.put("userMessage", userMessage);
        jsonobj_error_data.put("code", code);
        jsonobj_error_data.put("developerInfo", jsonarray_element);
        // OUTPUT AS pretty print JSON 
        return gson.toJson(jsonobj_error);
    }

    /**
     * TEXT ERROR MESSAGE for catch exceptions
     *
     * @param exception
     * @param endClass
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
