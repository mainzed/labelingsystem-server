package v1.rest;

import exceptions.Logging;
import java.io.File;
import java.text.SimpleDateFormat;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;

@Path("/")
public class RestResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getAPIpage() {
        try {
            JSONObject outObject = new JSONObject();
            outObject.put("title", "Labeling System API Version 1.0");
            outObject.put("version", "1.0");
            outObject.put("wiki", "https://github.com/labelingsystem/server/wiki");
            // get last modified data
            File file = new File(InfoResource.class.getClassLoader().getResource("config.properties").getFile());
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            outObject.put("last build", sdf.format(file.lastModified()));
            return Response.ok(outObject).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.RestResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

}
