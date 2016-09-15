package v1.rest;

import exceptions.Logging;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@Path("/languages")
public class LanguagesResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getAPIpage() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(LanguagesResource.class.getClassLoader().getResource("languages.json").getFile()), "UTF8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}
			br.close();
			JSONArray jsonArray = (JSONArray) new JSONParser().parse(response.toString());
			for (Object obj : jsonArray) {
				JSONObject tmp = (JSONObject) obj;
				String name = (String) tmp.get("name");
				String value = (String) tmp.get("value");
				switch (value) {
					case "de":
						tmp.put("orderValue", "0");
						break;
					case "en":
						tmp.put("orderValue", "1");
						break;
					case "fr":
						tmp.put("orderValue", "3");
						break;
					case "es":
						tmp.put("orderValue", "4");
						break;
					case "it":
						tmp.put("orderValue", "5");
						break;
					default:
						tmp.put("orderValue", name);
				}
			}
			return Response.ok(jsonArray).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.LanguagesResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

}
