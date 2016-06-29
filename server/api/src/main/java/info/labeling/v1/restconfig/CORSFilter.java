package info.labeling.v1.restconfig;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import info.labeling.v1.utils.PropertiesLocal;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CORSFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        String method = request.getMethod();
        try {
            if (method.equals("GET")) {
                response.getHttpHeaders().add("Access-Control-Allow-Origin", PropertiesLocal.getPropertyParam("get_origin"));
            } else {
                response.getHttpHeaders().add("Access-Control-Allow-Origin", PropertiesLocal.getPropertyParam("other_origin"));
                //response.getHttpHeaders().add("Access-Control-Allow-Origin", "null");
            }
            response.getHttpHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, PATCH");
            response.getHttpHeaders().add("Access-Control-Allow-Headers", "Content-Type, Accept");
            response.getHttpHeaders().add("Access-Control-Allow-Credentials", "false");
        } catch (IOException ex) {
            Logger.getLogger(CORSFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

}
