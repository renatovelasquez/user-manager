package org.commonjava.web.user.data;

import java.util.List;

import org.commonjava.web.user.model.Permission;
import org.commonjava.web.user.model.Role;
import org.commonjava.web.user.model.User;

public interface UserDataBackend
{

    UserNotificationContext createNotificationContext();

    void deleteUser( final String username )
        throws UserDataException;

    void deleteUser( final String username, final UserNotificationContext notificationContext )
        throws UserDataException;

    void deleteRole( final String name )
        throws UserDataException;

    void deleteRole( final String name, final UserNotificationContext notificationContext )
        throws UserDataException;

    void deletePermission( final String name )
        throws UserDataException;

    void deletePermission( final String name, final UserNotificationContext notificationContext )
        throws UserDataException;

    Role getRole( final String roleName );

    Permission getPermission( final String permissionName );

    User getUser( final String username );

    Permission savePermission( final Permission perm )
        throws UserDataException;

    Permission savePermission( final Permission perm,
                               final UserNotificationContext notificationContext )
        throws UserDataException;

    Role saveRole( final Role role )
        throws UserDataException;

    Role saveRole( final Role role, final UserNotificationContext notificationContext )
        throws UserDataException;

    User saveUser( final User user )
        throws UserDataException;

    User saveUser( final User user, UserNotificationContext notificationContext )
        throws UserDataException;

    List<Permission> getPermissions();

    List<Role> getRoles();

    List<User> getUsers();

    boolean hasPermission( String name );

    boolean hasRole( String name );

    boolean hasUser( String username );

}