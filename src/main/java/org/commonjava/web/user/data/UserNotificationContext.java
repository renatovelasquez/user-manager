package org.commonjava.web.user.data;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Event;

import org.commonjava.web.user.model.Permission;
import org.commonjava.web.user.model.Role;
import org.commonjava.web.user.model.User;

public class UserNotificationContext
{

    private final Event<Set<User>> userEventSrc;

    private final Event<Set<Role>> roleEventSrc;

    private final Event<Set<Permission>> permissionEventSrc;

    private final Set<User> changedUsers = new HashSet<User>();

    private final Set<Role> changedRoles = new HashSet<Role>();

    private final Set<Permission> changedPermissions = new HashSet<Permission>();

    public UserNotificationContext( final Event<Set<User>> userEventSrc,
                                    final Event<Set<Role>> roleEventSrc,
                                    final Event<Set<Permission>> permissionEventSrc )
    {
        this.userEventSrc = userEventSrc;
        this.roleEventSrc = roleEventSrc;
        this.permissionEventSrc = permissionEventSrc;
    }

    public UserNotificationContext userChanged( final User user )
    {
        changedUsers.add( user );
        return this;
    }

    public UserNotificationContext roleChanged( final Role role )
    {
        changedRoles.add( role );
        return this;
    }

    public UserNotificationContext permissionChanged( final Permission perm )
    {
        changedPermissions.add( perm );
        return this;
    }

    public void sendNotifications()
    {
        if ( !changedUsers.isEmpty() )
        {
            userEventSrc.fire( changedUsers );
        }

        if ( !changedRoles.isEmpty() )
        {
            roleEventSrc.fire( changedRoles );
        }

        if ( !changedPermissions.isEmpty() )
        {
            permissionEventSrc.fire( changedPermissions );
        }

    }

}
