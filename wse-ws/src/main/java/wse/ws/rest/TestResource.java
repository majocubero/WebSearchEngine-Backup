package wse.ws.rest;

import flexjson.JSONSerializer;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ri.wse.core.result.service.ResultService;
import ri.wse.model.QueryResult;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component
@Scope("request")
@Path("/query")
public class TestResource {

    @Autowired
    ResultService service;

    @GET
    @Path("/result")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testService(@QueryParam("query") String query) {
        System.out.println("entre");

        if (StringUtils.isEmpty(query)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        QueryResult queryResult = this.service.getResults(query);
        if (queryResult == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok().entity(new JSONSerializer().include("queryResults").serialize(queryResult)).build();
    }

}
