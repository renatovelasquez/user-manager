package org.commonjava.web.user.rest;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

import org.commonjava.util.logging.Logger;
import org.commonjava.web.common.model.Listing;
import org.commonjava.web.common.model.MappingArray;
import org.commonjava.web.user.data.UserDataException;
import org.commonjava.web.user.data.UserDataManager;
import org.commonjava.web.user.model.Role;
import org.commonjava.web.user.model.User;

@Path( "/users" )
@RequestScoped
// @RequiresAuthentication
public class UserResource
{

    private final Logger logger = new Logger( getClass() );

    @Inject
    private UserDataManager dataManager;

    @Context
    private UriInfo uriInfo;

    @GET
    @Path( "list" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public Listing<User> listUsers()
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( User.NAMESPACE, Permission.ADMIN ) );

        return new Listing<User>( dataManager.getUsers() );
    }

    @GET
    @Path( "{name}" )
    @Produces( { MediaType.APPLICATION_JSON } )
    public User getUser( @PathParam( "name" ) final String name )
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( User.NAMESPACE, Permission.ADMIN ) );

        return dataManager.getUser( name );
    }

    @POST
    @Consumes( { MediaType.APPLICATION_JSON } )
    public Response createUser( final User user )
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( User.NAMESPACE, Permission.ADMIN ) );

        // TODO: Validation!
        // final User user = element.getValue();
        // user.setUsername( name );

        ResponseBuilder builder;
        try
        {
            dataManager.createUser( user, dataManager.createContext(), true );
            builder =
                Response.created( uriInfo.getAbsolutePathBuilder().build( user.getUsername() ) );
        }
        catch ( final UserDataException e )
        {
            logger.error( "Failed to create user: %s. Reason: %s", e, user.getUsername(),
                          e.getMessage() );
            builder = Response.serverError();
        }

        return builder.build();
    }

    @POST
    @Path( "{name}/roles" )
    @Consumes( { MediaType.APPLICATION_JSON } )
    public Response updateRoles( @PathParam( "name" ) final String name,
                                 final JAXBElement<MappingArray> element )
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( User.NAMESPACE, Permission.ADMIN ) );

        final MappingArray roleNames = element.getValue();

        ResponseBuilder builder;

        final User user = dataManager.getUser( name );
        if ( user == null )
        {
            return Response.status( Status.BAD_REQUEST ).header( "Reason", "Invalid user: " + name ).build();
        }

        final Set<Role> userRoles = new HashSet<Role>();
        for ( final String roleName : roleNames )
        {
            final Role role = dataManager.getRole( roleName );
            if ( role == null )
            {
                return Response.status( Status.BAD_REQUEST ).header( "Reason",
                                                                     "Invalid role: " + roleName ).build();
            }

            userRoles.add( role );
        }

        user.setRoles( userRoles );

        try
        {
            dataManager.updateUser( user, dataManager.createContext(), true );
            builder =
                Response.ok().contentLocation( uriInfo.getAbsolutePathBuilder().build( name ) );
        }
        catch ( final UserDataException e )
        {
            logger.error( "Failed to update user: %s with roles: %s. Reason: %s", e, name,
                          roleNames, e.getMessage() );
            builder = Response.serverError();
        }

        return builder.build();
    }

    @POST
    @Path( "{name}" )
    @Consumes( { MediaType.APPLICATION_JSON } )
    public Response updateUser( @PathParam( "name" ) final String name,
                                final JAXBElement<User> element )
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( User.NAMESPACE, Permission.ADMIN ) );

        // TODO: Validation!
        final User user = element.getValue();
        user.setUsername( name );

        ResponseBuilder builder;
        try
        {
            dataManager.updateUser( user, dataManager.createContext(), true );
            builder =
                Response.ok().contentLocation( uriInfo.getAbsolutePathBuilder().build( name ) );
        }
        catch ( final UserDataException e )
        {
            logger.error( "Failed to create user: %s. Reason: %s", e, name, e.getMessage() );
            builder = Response.serverError();
        }

        return builder.build();
    }

    @DELETE
    @Path( "{name}" )
    public Response deleteUser( @PathParam( "name" ) final String name )
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( User.NAMESPACE, Permission.ADMIN ) );

        ResponseBuilder builder;
        try
        {
            dataManager.deleteUser( name, dataManager.createContext(), true );
            builder = Response.ok();
        }
        catch ( final UserDataException e )
        {
            logger.error( "Failed to remove user: %s. Reason: %s", e, name, e.getMessage() );
            builder = Response.serverError();
        }

        return builder.build();
    }
}
