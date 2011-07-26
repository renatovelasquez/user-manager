package org.commonjava.web.user.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.commonjava.util.logging.Logger;
import org.commonjava.web.common.model.Listing;
import org.commonjava.web.user.data.UserDataException;
import org.commonjava.web.user.data.UserDataManager;
import org.commonjava.web.user.model.Permission;

@Path( "/permissions" )
@RequestScoped
@RequiresAuthentication
public class PermissionResource
{

    private final Logger logger = new Logger( getClass() );

    @Inject
    private UserDataManager dataManager;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path( "list" )
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_XML } )
    public Listing<Permission> listPermissions()
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( Permission.NAMESPACE, Permission.ADMIN ) );

        return new Listing<Permission>( dataManager.getPermissions() );
    }

    @GET
    @Path( "{name}" )
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_XML } )
    public Permission getPermission( @PathParam( "name" )
    final String name )
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( Permission.NAMESPACE, Permission.ADMIN ) );

        return dataManager.getPermission( name );
    }

    @PUT
    @Path( "{name}" )
    public Response createPermission( @PathParam( "name" )
    final String name )
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( Permission.NAMESPACE, Permission.ADMIN ) );

        ResponseBuilder builder;
        try
        {
            dataManager.createPermission( name, true );
            builder = Response.created( uriInfo.getAbsolutePathBuilder().build( name ) );
        }
        catch ( final UserDataException e )
        {
            logger.error( "Failed to create permission: %s. Reason: %s", e, name, e.getMessage() );
            builder = Response.serverError();
        }

        return builder.build();
    }

    @DELETE
    @Path( "{name}" )
    public Response deletePermission( @PathParam( "name" )
    final String name )
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( Permission.NAMESPACE, Permission.ADMIN ) );

        ResponseBuilder builder;
        try
        {
            dataManager.deletePermission( name );
            builder = Response.ok();
        }
        catch ( final UserDataException e )
        {
            logger.error( "Failed to remove permission: %s. Reason: %s", e, name, e.getMessage() );
            builder = Response.serverError();
        }

        return builder.build();
    }

}
