package org.commonjava.web.user.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.commonjava.util.logging.Logger;
import org.commonjava.web.user.data.UserDataException;
import org.commonjava.web.user.data.UserDataInitializer;

@Path( "/manage" )
@RequestScoped
public class ManagementResource
{

    private final Logger logger = new Logger( getClass() );

    @Inject
    private UserDataInitializer userDataInitializer;

    @Path( "init" )
    @POST
    public Response reinitializeAdmin()
    {
        ResponseBuilder builder;
        try
        {
            userDataInitializer.initializeAdmin();
            builder = Response.ok();
        }
        catch ( UserDataException e )
        {
            logger.error( "Cannot reinitialize admin user/role/permissions. Error: %s", e,
                          e.getMessage() );
            builder = Response.serverError();
        }

        return builder.build();
    }

}
