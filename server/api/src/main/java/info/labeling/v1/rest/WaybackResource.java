package info.labeling.v1.rest;

import de.i3mainz.ls.rdfutils.exceptions.Logging;
import info.labeling.v1.utils.PropertiesLocal;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

@Path("/v1/wayback")
public class WaybackResource {

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getWaybackLink(@QueryParam("url") String url) {
		try {
			URL obj = new URL(PropertiesLocal.getPropertyParam("waybackapi").replace("$url", url));
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			String urlParameters = "";
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF8"));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// init output
			JSONObject jsonOut = new JSONObject();
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());
			JSONObject resultsObject = (JSONObject) jsonObject.get("archived_snapshots");
			JSONObject resultsObject2 = (JSONObject) resultsObject.get("closest");
			String wburl = null;
			try {
				wburl = (String) resultsObject2.get("url");
			} catch (Exception e) {
				throw new NullPointerException("no url available");
			}
			jsonOut.put("url", wburl);
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.NOT_FOUND).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.WaybackResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

}