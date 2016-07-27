package info.labeling.v1.rest;

import info.labeling.exceptions.Logging;
import info.labeling.v1.utils.Funcs;
import info.labeling.v1.utils.SQlite;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;

@Path("/auth")
public class AuthResource {

    @POST
    @Path("/verify")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response verifyUser(@FormParam("user") String user, @FormParam("pwd") String pwd) {
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

    @POST
    @Path("/newuser")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response addNewUser(@FormParam("user") String user, @FormParam("pwd") String pwd) {
        try {
            Boolean res = SQlite.insertUser(user, pwd);
            JSONObject jsonOut = new JSONObject();
            jsonOut.put("status", res);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AuthResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }
    
    @POST
    @Path("/deactivate")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response deactivateUser(@FormParam("user") String user) {
        try {
            Boolean res = SQlite.deactivateUser(user);
            JSONObject jsonOut = new JSONObject();
            jsonOut.put("status", res);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AuthResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }
    
    @POST
    @Path("/activate")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response activateUser(@FormParam("user") String user) {
        try {
            Boolean res = SQlite.activateUser(user);
            JSONObject jsonOut = new JSONObject();
            jsonOut.put("status", res);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AuthResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }
    
    @POST
    @Path("/hash")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getHash(@FormParam("str") String str) {
        try {
            String salt = Funcs.generateHash();
            String hash = salt + Funcs.SHA1(salt + str);
            JSONObject jsonOut = new JSONObject();
            jsonOut.put("hash", hash);
            return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.AuthResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

}
