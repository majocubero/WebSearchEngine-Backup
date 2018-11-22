package wse.ws.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ri.wse.core.result.ResultService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


@Component
@Scope("request")
@Path("/test")
public class ResultResource {

    @Autowired
    private ResultService service;

    @GET
    public Response testNLU(){
        return Response.ok().entity(this.service.sayHello()).build();
    }
}
