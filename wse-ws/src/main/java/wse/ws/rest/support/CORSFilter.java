package wse.ws.rest.support;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

/**
 * CORS (Cross-Origin Resource Sharing) filter configuration.
 *
 *@author María José Cubero.
 */
public class CORSFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        String containerResponseOrigin = request.getHeaderValue("Origin");
        if(containerResponseOrigin != null){
            response.getHttpHeaders().add("Access-Control-Allow-Origin", containerResponseOrigin);
            response.getHttpHeaders().add("Access-Control-Allow-Credentials", "true");
            response.getHttpHeaders().add("Access-Control-Allow-Headers", "content-length, content-type, origin, authorization");
            response.getHttpHeaders().add("Access-Control-Allow-Methods","GET, POST, DELETE, PUT, OPTIONS, ACCEPT");

        }
        return response;
    }
}
