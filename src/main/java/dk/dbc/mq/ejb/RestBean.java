package dk.dbc.mq.ejb;

import dk.dbc.mq.CowBrow;
import dk.dbc.mq.json.ResultJSON;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("/")
public class RestBean {
    public static final String QUERY_LOGIN = "login";
    public static final String QUERY_GET_DESTIONATIONS = "destinations";
    public static final String QUERY_GET_MESSAGES = "messages";
    public static final String QUERY_CREATE_QUEUE = "create";
    public static final String QUERY_DESTROY_QUEUE = "destroy";
    public static final String QUERY_SEND_TEXT = "sendtext";

    public static final String QUERYPARAM_HOST = "host";
    public static final String QUERYPARAM_PORT = "port";
    public static final String QUERYPARAM_USER = "user";
    public static final String QUERYPARAM_PASSWORD = "password";
    public static final String QUERYPARAM_QUEUE = "queue";
    public static final String QUERYPARAM_TEXT = "text";

    @Inject
    ContextBean context;

    @GET
    @Path(QUERY_LOGIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response login(@QueryParam(QUERYPARAM_HOST) String host,
            @DefaultValue(CowBrow.DEFAULT_PORT) @QueryParam(QUERYPARAM_PORT) String port,
            @DefaultValue(CowBrow.DEFAULT_USER) @QueryParam(QUERYPARAM_USER) String user,
            @DefaultValue(CowBrow.DEFAULT_PASSWORD) @QueryParam(QUERYPARAM_PASSWORD) String password) {
        if(!context.login(host, port, user, password))
            return Response.serverError().entity(ResultJSON.writeErrorJson(
                500, "starting jms session failed")).build();
        return Response.ok().build();
    }

    @GET
    @Path(QUERY_GET_DESTIONATIONS)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQueues() {
        String response = context.getQueues();
        return Response.ok().entity(response).build();
    }

    @GET
    @Path(QUERY_GET_MESSAGES)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMessages(@QueryParam(QUERYPARAM_QUEUE) String queue) {
        String response = context.getMessages(queue);
        return Response.ok().entity(response).build();
    }

    @GET
    @Path(QUERY_CREATE_QUEUE)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createQueue(@QueryParam(QUERYPARAM_QUEUE) String queue) {
        String response = context.createQueue(queue);
        return Response.ok().entity(response).build();
    }

    @GET
    @Path(QUERY_DESTROY_QUEUE)
    @Produces(MediaType.APPLICATION_JSON)
    public Response destroyQueue(@QueryParam(QUERYPARAM_QUEUE) String queue) {
        String response = context.destroyQueue(queue);
        return Response.ok().entity(response).build();
    }

    @POST
    @Path(QUERY_SEND_TEXT)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendText(@QueryParam(QUERYPARAM_QUEUE) String queue,
           @QueryParam(QUERYPARAM_TEXT) String text, String propertiesJson) {
        String response = context.sendText(queue, text, propertiesJson);
        return Response.ok().entity(response).build();
    }
}
