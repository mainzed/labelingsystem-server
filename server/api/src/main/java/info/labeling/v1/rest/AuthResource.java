package info.labeling.v1.rest;

import de.i3mainz.ls.rdfutils.exceptions.Logging;
import info.labeling.v1.utils.SQlite;
import java.net.URL;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;

@Path("/v1/auth")
public class AuthResource {

	@POST
	@Path("/verify")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getWaybackLink(@FormParam("user") String user, @FormParam("pwd") String pwd) {
		try {
			String role = SQlite.getUserInfoAndCheckPassword(user, pwd);
			JSONObject jsonOut = new JSONObject();
			jsonOut.put("role", role);
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			if (e.toString().contains("AccessDeniedException")) {
                return Response.status(Response.Status.FORBIDDEN).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AuthResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AuthResource"))
                        .header("Content-Type", "application/json;charset=UTF-8").build();
            }
		}
	}

}