package org.commonjava.web.user.data;

import java.util.List;

import org.commonjava.web.user.model.Permission;
import org.commonjava.web.user.model.Role;
import org.commonjava.web.user.model.User;

public interface UserDataBackend
{

    UserDataContext createContext();

    void deleteUser( final String username, final UserDataContext context )
        throws UserDataException;

    void deleteRole( final String name, final UserDataContext context )
        throws UserDataException;

    void deletePermission( final String name, final UserDataContext context )
        throws UserDataException;

    Role getRole( final String roleName );

    Permission getPermission( final String permissionName );

    User getUser( final String username );

    Permission savePermission( final Permission perm, final UserDataContext context )
        throws UserDataException;

    Role saveRole( final Role role, final UserDataContext context )
        throws UserDataException;

    User saveUser( final User user, final UserDataContext context )
        throws UserDataException;

    List<Permission> getPermissions();

    List<Role> getRoles();

    List<User> getUsers();

    boolean hasPermission( String name );

    boolean hasRole( String name );

    boolean hasUser( String username );

}