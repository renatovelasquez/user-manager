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
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

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

    @Inject
    private UserTransaction tx;

    private List<User> users;

    private List<Role> roles;

    private List<Permission> permissions;

    // @Inject
    // private PostOffice mailManager;

    @Inject
    private PasswordManager passwordManager;

    public UserNotificationContext createNotificationContext()
    {
        return backend.createNotificationContext();
    }

    public Permission createPermission( final String name )
        throws UserDataException
    {
        return createPermission( name, null );
    }

    public Permission createPermission( final String name, final UserNotificationContext context )
        throws UserDataException
    {
        if ( backend.hasPermission( name ) )
        {
            throw new UserDataException( "Permission already exists: %s", name );
        }

        return backend.savePermission( new Permission( name ), context );
    }

    public Permission createPermission( final Permission perm )
        throws UserDataException
    {
        return createPermission( perm, null );
    }

    public Permission createPermission( final Permission perm, final UserNotificationContext context )
        throws UserDataException
    {
        if ( backend.hasPermission( perm.getName() ) )
        {
            throw new UserDataException( "Permission already exists: %s", perm.getName() );
        }

        return backend.savePermission( perm, context );
    }

    public Role createRole( final String name )
        throws UserDataException
    {
        return createRole( name, null );
    }

    public Role createRole( final String name, final UserNotificationContext context )
        throws UserDataException
    {
        if ( backend.hasRole( name ) )
        {
            throw new UserDataException( "Role already exists: %s", name );
        }

        return backend.saveRole( new Role( name ), context );
    }

    public Role createRole( final Role role )
        throws UserDataException
    {
        return createRole( role, null );
    }

    public Role createRole( final Role role, final UserNotificationContext context )
        throws UserDataException
    {
        if ( backend.hasRole( role.getName() ) )
        {
            throw new UserDataException( "Role already exists: %s", role.getName() );
        }

        return backend.saveRole( role, context );
    }

    public Role updateRole( final Role role )
        throws UserDataException
    {
        return updateRole( role, null );
    }

    public Role updateRole( final Role role, final UserNotificationContext context )
        throws UserDataException
    {
        if ( !backend.hasRole( role.getName() ) )
        {
            throw new UserDataException( "Role doesn't exist: %s", role.getName() );
        }

        Role existing = backend.getRole( role.getName() );
        if ( role != existing )
        {
            existing = existing.updateFrom( role );
        }

        return backend.saveRole( existing, context );
    }

    public User createUser( final User user )
        throws UserDataException
    {
        return createUser( user, null );
    }

    public User createUser( final User user, final UserNotificationContext context )
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

        return backend.saveUser( user, context );
    }

    public User updateUser( final User user )
        throws UserDataException
    {
        return updateUser( user, null );
    }

    public User updateUser( final User user, final UserNotificationContext context )
        throws UserDataException
    {
        if ( !backend.hasUser( user.getUsername() ) )
        {
            throw new UserDataException( "User doesn't exist: %s", user.getUsername() );
        }

        User existing = getUser( user.getUsername() );
        if ( user != existing )
        {
            existing = existing.updateFrom( user );
        }

        return backend.saveUser( existing, context );
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

    public Map<String, Permission> createCRUDPermissions( final String namespace, final String name )
        throws UserDataException
    {
        return createCRUDPermissions( namespace, name, null );
    }

    public Map<String, Permission> createCRUDPermissions( final String namespace,
                                                          final String name,
                                                          UserNotificationContext context )
        throws UserDataException
    {
        boolean autoCommit = context == null;

        if ( context == null )
        {
            context = backend.createNotificationContext();
        }

        try
        {
            if ( autoCommit )
            {
                tx.begin();
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
                tx.commit();
                context.sendNotifications();
            }

            return perms;
        }
        catch ( final NotSupportedException e )
        {
            throw new UserDataException( "Cannot create CRUD permissions for: %s:%s. Error: %s", e,
                                         namespace, name, e.getMessage() );
        }
        catch ( final SystemException e )
        {
            throw new UserDataException( "Cannot create CRUD permissions for: %s:%s. Error: %s", e,
                                         namespace, name, e.getMessage() );
        }
        catch ( final RollbackException e )
        {
            throw new UserDataException( "Cannot create CRUD permissions for: %s:%s. Error: %s", e,
                                         namespace, name, e.getMessage() );
        }
        catch ( final HeuristicMixedException e )
        {
            throw new UserDataException( "Cannot create CRUD permissions for: %s:%s. Error: %s", e,
                                         namespace, name, e.getMessage() );
        }
        catch ( final HeuristicRollbackException e )
        {
            throw new UserDataException( "Cannot create CRUD permissions for: %s:%s. Error: %s", e,
                                         namespace, name, e.getMessage() );
        }
    }

    public void deletePermission( final String name )
        throws UserDataException
    {
        deletePermission( name, null );
    }

    public void deletePermission( final String name, UserNotificationContext context )
        throws UserDataException
    {
        boolean autoCommit = context == null;

        if ( context == null )
        {
            context = backend.createNotificationContext();
        }
        try
        {
            if ( autoCommit )
            {
                tx.begin();
            }

            for ( Role role : roles )
            {
                Permission perm = new Permission( name );
                if ( role.removePermission( perm ) )
                {
                    backend.saveRole( role, context );
                }
            }

            backend.deletePermission( name, context );

            if ( autoCommit )
            {
                tx.commit();
                context.sendNotifications();
            }
        }
        catch ( final NotSupportedException e )
        {
            throw new UserDataException( "Cannot remove role: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( final SystemException e )
        {
            throw new UserDataException( "Cannot remove role: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( final RollbackException e )
        {
            throw new UserDataException( "Cannot remove role: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( final HeuristicMixedException e )
        {
            throw new UserDataException( "Cannot remove role: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( final HeuristicRollbackException e )
        {
            throw new UserDataException( "Cannot remove role: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
    }

    public void deleteRole( final String name )
        throws UserDataException
    {
        deleteRole( name, null );
    }

    public void deleteRole( final String name, UserNotificationContext context )
        throws UserDataException
    {
        boolean autoCommit = context == null;

        if ( context == null )
        {
            context = backend.createNotificationContext();
        }
        try
        {
            if ( autoCommit )
            {
                tx.begin();
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
                tx.commit();
                context.sendNotifications();
            }
        }
        catch ( final NotSupportedException e )
        {
            throw new UserDataException( "Cannot remove user: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( final SystemException e )
        {
            throw new UserDataException( "Cannot remove user: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( final RollbackException e )
        {
            throw new UserDataException( "Cannot remove user: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( final HeuristicMixedException e )
        {
            throw new UserDataException( "Cannot remove user: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( final HeuristicRollbackException e )
        {
            throw new UserDataException( "Cannot remove user: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
    }

    public void deleteUser( final String username )
        throws UserDataException
    {
        deleteUser( username, null );
    }

    public void deleteUser( final String username, final UserNotificationContext context )
        throws UserDataException
    {
        backend.deleteUser( username, context );
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
