package org.commonjava.web.user.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.commonjava.web.common.model.Listing;
import org.commonjava.web.user.data.UserDataManager;
import org.commonjava.web.user.model.Role;

@Path( "/roles" )
@RequestScoped
@RequiresAuthentication
public class RoleResource
{

    // private final Logger logger = new Logger( getClass() );

    @Inject
    private UserDataManager dataManager;

    @GET
    @Path( "list" )
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_XML } )
    public Listing<Role> listRoles()
    {
        // FIXME: Un-comment this!!
        // SecurityUtils.getSubject()
        // .checkPermission( Permission.name( Role.NAMESPACE, Permission.ADMIN ) );

        return new Listing<Role>( dataManager.getRoles() );
    }

}
