package org.commonjava.web.user.rest;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBElement;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.commonjava.util.logging.Logger;
import org.commonjava.web.common.model.Listing;
import org.commonjava.web.common.model.MappingArray;
import org.commonjava.web.user.data.UserDataException;
import org.commonjava.web.user.data.UserDataManager;
import org.commonjava.web.user.model.Permission;
import org.commonjava.web.user.model.Role;
import org.commonjava.web.user.model.SimpleRole;

@Path( "/roles" )
@RequestScoped
@RequiresAuthentication
public class RoleResource
{

    private final Logger logger = new Logger( getClass() );

    @Inject
    private UserDataManager dataManager;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path( "list" )
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_XML } )
    public Listing<SimpleRole> listRoles()
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( Role.NAMESPACE, Permission.ADMIN ) );

        return new Listing<SimpleRole>( SimpleRole.convert( dataManager.getRoles() ) );
    }

    @POST
    @Path( "{name}/permissions" )
    public Response updatePermissions( @PathParam( "name" ) final String name, final JAXBElement<MappingArray> element )
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( Role.NAMESPACE, Permission.ADMIN ) );

        final MappingArray permissionNames = element.getValue();

        ResponseBuilder builder;

        final Role role = dataManager.getRole( name );
        if ( role == null )
        {
            return Response.status( Status.BAD_REQUEST )
                           .header( "Reason", "Invalid role: " + name )
                           .build();
        }

        final Set<Permission> rolePermissions = new HashSet<Permission>();
        for ( final String permName : permissionNames )
        {
            final Permission perm = dataManager.getPermission( permName );
            if ( perm == null )
            {
                return Response.status( Status.BAD_REQUEST )
                               .header( "Reason", "Invalid permission: " + permName )
                               .build();
            }

            rolePermissions.add( perm );
        }

        role.setPermissions( rolePermissions );

        try
        {
            dataManager.updateRole( role, true );
            builder = Response.ok()
                              .contentLocation( uriInfo.getAbsolutePathBuilder()
                                                       .build( name ) );
        }
        catch ( final UserDataException e )
        {
            logger.error( "Failed to update role: %s with permissions: %s. Reason: %s", e, name, permissionNames,
                          e.getMessage() );
            builder = Response.serverError();
        }

        return builder.build();
    }

    @GET
    @Path( "{name}" )
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_XML } )
    public Role getRole( @PathParam( "name" ) final String name )
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( Role.NAMESPACE, Permission.ADMIN ) );

        return dataManager.getRole( name );
    }

    @PUT
    @Path( "{name}" )
    public Response createRole( @PathParam( "name" ) final String name )
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( Role.NAMESPACE, Permission.ADMIN ) );

        ResponseBuilder builder;
        try
        {
            dataManager.createRole( name, true );
            builder = Response.created( uriInfo.getAbsolutePathBuilder()
                                               .build( name ) );
        }
        catch ( final UserDataException e )
        {
            logger.error( "Failed to create role: %s. Reason: %s", e, name, e.getMessage() );
            builder = Response.serverError();
        }

        return builder.build();
    }

    @DELETE
    @Path( "{name}" )
    public Response deleteRole( @PathParam( "name" ) final String name )
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( Role.NAMESPACE, Permission.ADMIN ) );

        ResponseBuilder builder;
        try
        {
            dataManager.deleteRole( name );
            builder = Response.ok();
        }
        catch ( final UserDataException e )
        {
            logger.error( "Failed to remove role: %s. Reason: %s", e, name, e.getMessage() );
            builder = Response.serverError();
        }

        return builder.build();
    }

}
