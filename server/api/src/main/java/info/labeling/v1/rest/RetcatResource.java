package info.labeling.v1.rest;

import de.i3mainz.ls.rdfutils.exceptions.Logging;
import info.labeling.v1.utils.PropertiesLocal;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Path("/v1/retcat")
public class RetcatResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public Response getRetcatList() {
		try {
			return Response.ok(getRetcatListAll()).header("Content-Type", "application/json;charset=UTF-8").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.RetcatResource"))
					.header("Content-Type", "application/json;charset=UTF-8").build();
		}
	}

	private JSONObject getRetcatListAll() throws IOException {
		JSONObject jsonRETCAT = new JSONObject();
		JSONArray outArray = new JSONArray();
		// add items
		JSONObject tmpRETCAT = new JSONObject();
		tmpRETCAT.put("name", "Getty AAT");
		tmpRETCAT.put("fulltextquery", PropertiesLocal.getPropertyParam("api") + "/v1/autosuggests/getty/aat");
		tmpRETCAT.put("labelquery", PropertiesLocal.getPropertyParam("api") + "/v1/resdetail/getty");
		outArray.add(tmpRETCAT);
		tmpRETCAT = new JSONObject();
		tmpRETCAT.put("name", "Getty TGN");
		tmpRETCAT.put("fulltextquery", PropertiesLocal.getPropertyParam("api") + "/v1/autosuggests/getty/tgn");
		tmpRETCAT.put("labelquery", PropertiesLocal.getPropertyParam("api") + "/v1/resdetail/getty");
		outArray.add(tmpRETCAT);
		tmpRETCAT = new JSONObject();
		tmpRETCAT.put("name", "Getty ULAN");
		tmpRETCAT.put("fulltextquery", PropertiesLocal.getPropertyParam("api") + "/v1/autosuggests/getty/ulan");
		tmpRETCAT.put("labelquery", PropertiesLocal.getPropertyParam("api") + "/v1/resdetail/getty");
		outArray.add(tmpRETCAT);
		tmpRETCAT = new JSONObject();
		tmpRETCAT.put("name", "Heritage Data Historic England");
		tmpRETCAT.put("fulltextquery", PropertiesLocal.getPropertyParam("api") + "/v1/autosuggests/heritagedata/historicengland");
		tmpRETCAT.put("labelquery", PropertiesLocal.getPropertyParam("api") + "/v1/resdetail/heritagedata");
		outArray.add(tmpRETCAT);
		tmpRETCAT = new JSONObject();
		tmpRETCAT.put("name", "DBpedia");
		tmpRETCAT.put("fulltextquery", PropertiesLocal.getPropertyParam("api") + "/v1/autosuggests/dbpedia");
		tmpRETCAT.put("labelquery", PropertiesLocal.getPropertyParam("api") + "/v1/resdetail/html");
		outArray.add(tmpRETCAT);
		// loop over all public vocabularies
		tmpRETCAT = new JSONObject();
		tmpRETCAT.put("name", "DBpedia");
		tmpRETCAT.put("fulltextquery", PropertiesLocal.getPropertyParam("api") + "/v1/autosuggests/dbpedia");
		tmpRETCAT.put("labelquery", PropertiesLocal.getPropertyParam("api") + "/v1/resdetail/html");
		outArray.add(tmpRETCAT);
		// output
		jsonRETCAT.put("retcat", outArray);
		return jsonRETCAT;
	}

}
