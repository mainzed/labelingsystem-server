package v1.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import link.labeling.retcat.rest.RetcatREST;
import org.json.simple.JSONObject;

@Path("/rtc")
public class RtcResource extends RetcatREST {

    public RtcResource() {
        super();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getInfo() {
        JSONObject out = new JSONObject();
        out.put("CoffeePot", "https://tools.ietf.org/html/rfc2324");
        return Response.status(418).entity(out).build();
    }

}
