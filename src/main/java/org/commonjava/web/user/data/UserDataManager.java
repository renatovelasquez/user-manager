/*******************************************************************************
 * Copyright (C) 2011 John Casey.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.commonjava.web.user.data;

import static org.commonjava.web.user.model.Permission.CREATE;
import static org.commonjava.web.user.model.Permission.DELETE;
import static org.commonjava.web.user.model.Permission.READ;
import static org.commonjava.web.user.model.Permission.UPDATE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.commonjava.util.logging.Logger;
import org.commonjava.web.user.model.Permission;
import org.commonjava.web.user.model.Role;
import org.commonjava.web.user.model.User;

@Singleton
public class UserDataManager
{
    private final Logger logger = new Logger( getClass() );

    @Inject
    private UserDataBackend backend;

    private List<User> users;

    private List<Role> roles;

    private List<Permission> permissions;

    // @Inject
    // private PostOffice mailManager;

    @Inject
    private PasswordManager passwordManager;

    public UserDataContext createContext()
    {
        return backend.createContext();
    }

    public Permission createPermission( final String name, final UserDataContext dataContext,
                                        final boolean autoCommit )
        throws UserDataException
    {
        if ( backend.hasPermission( name ) )
        {
            throw new UserDataException( "Permission already exists: %s", name );
        }

        try
        {
            if ( autoCommit )
            {
                dataContext.begin();
            }

            Permission p = backend.savePermission( new Permission( name ), dataContext );

            if ( autoCommit )
            {
                dataContext.commit();
                dataContext.sendNotifications();
            }

            return p;
        }
        catch ( UserDataException e )
        {
            dataContext.rollback();
            throw e;
        }
    }

    public Permission createPermission( final Permission perm, final UserDataContext dataContext,
                                        final boolean autoCommit )
        throws UserDataException
    {
        if ( backend.hasPermission( perm.getName() ) )
        {
            throw new UserDataException( "Permission already exists: %s", perm.getName() );
        }

        try
        {
            if ( autoCommit )
            {
                dataContext.begin();
            }

            Permission p = backend.savePermission( perm, dataContext );

            if ( autoCommit )
            {
                dataContext.commit();
                dataContext.sendNotifications();
            }

            return p;
        }
        catch ( UserDataException e )
        {
            dataContext.rollback();
            throw e;
        }
    }

    public Role createRole( final String name, final UserDataContext dataContext,
                            final boolean autoCommit )
        throws UserDataException
    {
        if ( backend.hasRole( name ) )
        {
            throw new UserDataException( "Role already exists: %s", name );
        }

        try
        {
            if ( autoCommit )
            {
                dataContext.begin();
            }

            Role r = backend.saveRole( new Role( name ), dataContext );

            if ( autoCommit )
            {
                dataContext.commit();
                dataContext.sendNotifications();
            }

            return r;
        }
        catch ( UserDataException e )
        {
            dataContext.rollback();
            throw e;
        }
    }

    public Role createRole( final Role role, final UserDataContext dataContext,
                            final boolean autoCommit )
        throws UserDataException
    {
        if ( backend.hasRole( role.getName() ) )
        {
            throw new UserDataException( "Role already exists: %s", role.getName() );
        }

        try
        {
            if ( autoCommit )
            {
                dataContext.begin();
            }

            Role r = backend.saveRole( role, dataContext );

            if ( autoCommit )
            {
                dataContext.commit();
                dataContext.sendNotifications();
            }

            return r;
        }
        catch ( UserDataException e )
        {
            dataContext.rollback();
            throw e;
        }
    }

    public Role updateRole( final Role role, final UserDataContext dataContext,
                            final boolean autoCommit )
        throws UserDataException
    {
        if ( !backend.hasRole( role.getName() ) )
        {
            throw new UserDataException( "Role doesn't exist: %s", role.getName() );
        }

        try
        {
            if ( autoCommit )
            {
                dataContext.begin();
            }

            Role r = backend.saveRole( role, dataContext );

            if ( autoCommit )
            {
                dataContext.commit();
                dataContext.sendNotifications();
            }

            return r;
        }
        catch ( UserDataException e )
        {
            dataContext.rollback();
            throw e;
        }
    }

    public User createUser( final User user, final UserDataContext dataContext,
                            final boolean autoCommit )
        throws UserDataException
    {
        if ( backend.hasUser( user.getUsername() ) )
        {
            throw new UserDataException( "User already exists: %s", user.getUsername() );
        }

        if ( user.getPasswordDigest() == null )
        {
            final String password = passwordManager.generatePassword();

            // final MailMessage message = new MailMessage( UserMailTemplates.NEW_USER.template(), user.getEmail() );
            // message.property( "user", user );
            // message.property( "password", password );

            // mailManager.sendMessage( message );

            logger.info( "Encrypting password." );
            user.setPasswordDigest( passwordManager.digestPassword( password ) );
        }

        try
        {
            if ( autoCommit )
            {
                dataContext.begin();
            }

            User u = backend.saveUser( user, dataContext );

            if ( autoCommit )
            {
                dataContext.commit();
                dataContext.sendNotifications();
            }

            return u;
        }
        catch ( UserDataException e )
        {
            dataContext.rollback();
            throw e;
        }
    }

    public User updateUser( final User user, final UserDataContext dataContext,
                            final boolean autoCommit )
        throws UserDataException
    {
        if ( !backend.hasUser( user.getUsername() ) )
        {
            throw new UserDataException( "User doesn't exist: %s", user.getUsername() );
        }

        try
        {
            if ( autoCommit )
            {
                dataContext.begin();
            }

            User u = backend.saveUser( user, dataContext );

            if ( autoCommit )
            {
                dataContext.commit();
                dataContext.sendNotifications();
            }

            return u;
        }
        catch ( UserDataException e )
        {
            dataContext.rollback();
            throw e;
        }
    }

    @Produces
    @Named
    public List<User> getUsers()
    {
        return users;
    }

    @Produces
    @Named
    public List<Role> getRoles()
    {
        return roles;
    }

    @Produces
    @Named
    public List<Permission> getPermissions()
    {
        return permissions;
    }

    public User getUser( final String username )
    {
        return backend.getUser( username );
    }

    public Permission getPermission( final String permissionName )
    {
        return backend.getPermission( permissionName );
    }

    public Role getRole( final String roleName )
    {
        return backend.getRole( roleName );
    }

    public Map<String, Permission> createCRUDPermissions( final String namespace,
                                                          final String name,
                                                          final UserDataContext context,
                                                          final boolean autoCommit )
        throws UserDataException
    {
        try
        {
            if ( autoCommit )
            {
                context.begin();
            }

            final Map<String, Permission> perms = new HashMap<String, Permission>();
            perms.put( CREATE, new Permission( namespace, name, CREATE ) );
            perms.put( READ, new Permission( namespace, name, READ ) );
            perms.put( UPDATE, new Permission( namespace, name, UPDATE ) );
            perms.put( DELETE, new Permission( namespace, name, DELETE ) );

            for ( final Permission perm : perms.values() )
            {
                backend.savePermission( perm, context );
            }

            if ( autoCommit )
            {
                context.commit();
                context.sendNotifications();
            }

            return perms;
        }
        catch ( UserDataException e )
        {
            context.rollback();
            throw e;
        }
    }

    public void deletePermission( final String name, final UserDataContext context,
                                  final boolean autoCommit )
        throws UserDataException
    {
        try
        {
            if ( autoCommit )
            {
                context.begin();
            }

            Permission perm = new Permission( name );
            for ( Role role : roles )
            {
                logger.info( "\n\n\n\nChecking Role: %s\n\nfor permission: '%s'\n\n\n\n", role,
                             perm );

                if ( role.removePermission( perm ) )
                {
                    logger.info( "\n\n\n\nUpdating Role: %s to remove permission: %s\n\n\n\n",
                                 role, perm );
                    backend.saveRole( role, context );
                }

                logger.info( "\n\n\n\nAfter removal attempt, Role is: %s\n\n\n\n", role );
            }

            logger.info( "\n\n\n\nRemoving permission: %s\n\n\n\n", perm );
            backend.deletePermission( name, context );

            if ( autoCommit )
            {
                context.commit();
                context.sendNotifications();
            }
        }
        catch ( UserDataException e )
        {
            context.rollback();
            throw e;
        }
    }

    public void deleteRole( final String name, final UserDataContext context,
                            final boolean autoCommit )
        throws UserDataException
    {
        try
        {
            if ( autoCommit )
            {
                context.begin();
            }

            for ( User user : users )
            {
                Role role = new Role( name );
                if ( user.removeRole( role ) )
                {
                    backend.saveUser( user, context );
                }
            }

            backend.deleteRole( name, context );

            if ( autoCommit )
            {
                context.commit();
                context.sendNotifications();
            }
        }
        catch ( UserDataException e )
        {
            context.rollback();
            throw e;
        }
    }

    public void deleteUser( final String username, final UserDataContext dataContext,
                            final boolean autoCommit )
        throws UserDataException
    {
        try
        {
            if ( autoCommit )
            {
                dataContext.begin();
            }

            backend.deleteUser( username, dataContext );

            if ( autoCommit )
            {
                dataContext.commit();
                dataContext.sendNotifications();
            }
        }
        catch ( UserDataException e )
        {
            dataContext.rollback();
            throw e;
        }
    }

    public synchronized void onUserChanged( @Observes( notifyObserver = Reception.IF_EXISTS ) final User user )
    {
        users = null;
        loadData();
    }

    public synchronized void onRoleChanged( @Observes( notifyObserver = Reception.IF_EXISTS ) final Role role )
    {
        roles = null;
        loadData();
    }

    public synchronized void onPermissionChanged( @Observes( notifyObserver = Reception.IF_EXISTS ) final Permission perm )
    {
        permissions = null;
        loadData();
    }

    @PostConstruct
    public synchronized void loadData()
    {
        logger.info( "\n\n\n\n(Re)Loading user/role/permission data." );
        if ( users == null )
        {
            users = backend.getUsers();
        }

        if ( roles == null )
        {
            roles = backend.getRoles();
        }

        if ( permissions == null )
        {
            permissions = backend.getPermissions();
        }
    }

}
