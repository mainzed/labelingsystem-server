package info.labeling.v1.rest;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import de.i3mainz.ls.rdfutils.exceptions.Logging;
import info.labeling.v1.utils.CSV;
import info.labeling.v1.utils.PropertiesLocal;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/importcsv")
public class ImportcsvResource {

    public static double status = -1.0;
    public static String action = "";
    public static String creator = null;
    public static boolean preflabel = false;
    public static boolean validator = true;
    public static String csvContent = "";
    public static String mode = null;
    public static int maxSteps = -1;
    public static int currentStep = -1;
    public static String CONTEXT = null;
    public static String FILENAME = null;
    public static String FILELINK = null;
    public String outString = "";

    public static String SERVER_UPLOAD_LOCATION_FOLDER = "";
    private static String SHARE_WEB = "";

    // https://examples.javacodegeeks.com/enterprise-java/rest/jersey/jersey-file-upload-example/
    @POST
    @Path("/mode/{mode}/creator/{creator}/validator/{validator}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response csvUpload(@FormDataParam("fileName") InputStream fileInputStream, @FormDataParam("fileName") FormDataContentDisposition contentDispositionHeader,
            @PathParam("mode") String MODE, @PathParam("creator") String CREATOR, @PathParam("validator") String VALIDATOR) {
        try {
            if (MODE.equals("start")) {
                mode = MODE;
                creator = CREATOR;
                validator = Boolean.valueOf(VALIDATOR);
                SHARE_WEB = PropertiesLocal.getPropertyParam("share_web");
                SERVER_UPLOAD_LOCATION_FOLDER = PropertiesLocal.getPropertyParam("share_server");
            }
            CONTEXT = String.valueOf(System.currentTimeMillis());
            FILENAME = CONTEXT + "_" + CREATOR.replace(" ", "") + ".ttl";
            FILELINK = SHARE_WEB + FILENAME;
            String filePath = SERVER_UPLOAD_LOCATION_FOLDER + contentDispositionHeader.getFileName();
            // save the file to the server
            saveFile(fileInputStream, filePath);
            outString = "File saved to server location : " + filePath;
            String line = "";
            File datei = new File(filePath);
            FileInputStream fis = new FileInputStream(filePath);
            InputStreamReader isr = new InputStreamReader(fis, "UTF8");
            BufferedReader br = new BufferedReader(isr);
            csvContent = "";
            while ((line = br.readLine()) != null) {
                csvContent += line + "\r\n";
            }
            br.close();
            isr.close();
            fis.close();
            if (datei.exists()) {
                datei.delete();
            }
            start();
            return Response.status(200).entity(outString).header("Content-Type", "application/json;charset=UTF-8").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.ImportcsvResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    @GET
    @Path("/mode/{mode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response csvUploadUpdate(@PathParam("mode") String MODE) {
        try {
            if (MODE.equals("update")) {
                update();
                return Response.status(200).entity(outString).header("Content-Type", "application/json;charset=UTF-8").build();
            } else if (MODE.equals("finish")) {
                finish();
                return Response.status(200).entity(outString).header("Content-Type", "application/json;charset=UTF-8").build();
            } else {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Logging.getMessageJSON(e, "info.labeling.v1.rest.ImportcsvResource"))
                    .header("Content-Type", "application/json;charset=UTF-8").build();
        }
    }

    public void start() {
        status = 0.0;
        currentStep = 0;
        action = "start parsing...";
        System.out.println("=================================");
        String[] csvLines = csvContent.split("\r\n");
        if (validator) {
            maxSteps = (csvLines.length - 1) * 2; // 100% (2 = label + relation)
            System.out.println("csv-test");
        } else {
            maxSteps = (csvLines.length - 1) * 4; // 100% (4 = label + relation + label (validate) + relation (validate))
            System.out.println("csv-input");
        }
        (new Thread(new CSV())).start();
        update();
    }

    public void update() {
        outString = "{ \"status\": \"" + status + "\",  \"action\": \"" + action + "\"}";
    }

    public void finish() {
        outString = CSV.JSON_STRING;
    }

    private void saveFile(InputStream uploadedInputStream, String serverLocation) {
        try {
            OutputStream outpuStream = new FileOutputStream(new File(serverLocation));
            int read = 0;
            byte[] bytes = new byte[1024];
            outpuStream = new FileOutputStream(new File(serverLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                outpuStream.write(bytes, 0, read);
            }
            outpuStream.flush();
            outpuStream.close();
        } catch (IOException e) {
        }
    }

}
