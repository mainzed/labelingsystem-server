package v1.rest;

import exceptions.Logging;
import v1.utils.crypt.Crypt;
import v1.utils.db.SQlite;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import v1.utils.uuid.UniqueIdentifier;

@Path("/auth")
public class AuthResource {

	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response loginUser(@FormParam("user") String user, @FormParam("pwd") String pwd) {
		JSONObject jsonOut = new JSONObject();
		try {
			String secretToken = UniqueIdentifier.getUUID();
			String role = SQlite.getUserInfoAndCheckPassword(user, pwd);
			boolean login = SQlite.setLogin(user + ";" + secretToken, role);
			if (login) {
				String status[] = SQlite.getLoginStatus(user + ";" + secretToken);
				jsonOut.put("verified", true);
				jsonOut.put("user", user);
				jsonOut.put("role", status[0]);
				jsonOut.put("date", status[1]);
				jsonOut.put("token", secretToken);
			}
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			if (e.toString().contains("AccessDeniedException")) {
				return Response.status(Response.Status.FORBIDDEN).entity(Logging.getMessageJSON(e, "v1.rest.AuthResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			} else {
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.AuthResource"))
						.header("Content-Type", "application/json;charset=UTF-8").build();
			}
		}
	}

	@GET
	@Path("/status")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response statusUser(@QueryParam("user") String user, @QueryParam("token") String token) {
		JSONObject jsonOut = new JSONObject();
		try {
			String status[] = SQlite.getLoginStatus(user + ";" + token);
			if (status[0] != null) {
				jsonOut.put("verified", true);
				jsonOut.put("user", user);
				jsonOut.put("role", status[0]);
				jsonOut.put("date", status[1]);
			} else {
				jsonOut.put("verified", false);
				jsonOut.put("user", user);
			}
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.AuthResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@POST
	@Path("/logout")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response logoutUser(@FormParam("user") String user) {
		JSONObject jsonOut = new JSONObject();
		try {
			boolean logout = SQlite.setLogout(user);
			if (logout) {
				jsonOut.put("verified", false);
				jsonOut.put("user", user);
			}
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.AuthResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@POST
	@Path("/newuser")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response addNewUser(@FormParam("user") String user, @FormParam("pwd") String pwd) {
		try {
			Boolean res = SQlite.insertUser(user, pwd);
			JSONObject jsonOut = new JSONObject();
			return Response.status(Response.Status.CREATED).entity(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.AuthResource"))
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
			return Response.status(Response.Status.CREATED).entity(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.AuthResource"))
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
			return Response.status(Response.Status.CREATED).entity(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.AuthResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@POST
	@Path("/hash")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getHash(@FormParam("str") String str) {
		try {
			String salt = Crypt.generateHash();
			String hash = salt + Crypt.SHA1(salt + str);
			JSONObject jsonOut = new JSONObject();
			jsonOut.put("hash", hash);
			return Response.ok(jsonOut).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.AuthResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	@GET
	@Path("/users")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getUserList(@QueryParam("user") String user) {
		try {
			JSONArray users = SQlite.getUsersInfo();
			return Response.ok(users).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "v1.rest.AuthResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

}
