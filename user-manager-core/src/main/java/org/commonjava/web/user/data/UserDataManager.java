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

    @SuppressWarnings( "rawtypes" )
    @Inject
    private UserDataFactory factory;

    @Inject
    private UserTransaction tx;

    // @Inject
    // private PostOffice mailManager;

    @Inject
    private PasswordManager passwordManager;

    public Permission createPermission( final String name, final boolean autoCommit )
        throws UserDataException
    {
        if ( backend.hasPermission( name ) )
        {
            throw new UserDataException( "Permission already exists: %s", name );
        }

        return backend.savePermission( factory.newPermission( name ), autoCommit );
    }

    public Permission createPermission( final Permission perm, final boolean autoCommit )
        throws UserDataException
    {
        if ( backend.hasPermission( perm.getName() ) )
        {
            throw new UserDataException( "Permission already exists: %s", perm.getName() );
        }

        return backend.savePermission( perm, autoCommit );
    }

    public Permission updatePermission( final Permission perm, final boolean autoCommit )
        throws UserDataException
    {
        if ( !backend.hasPermission( perm.getName() ) )
        {
            throw new UserDataException( "Permission doesn't exist: %s", perm.getName() );
        }

        Permission existing = backend.getPermission( perm.getName() );
        if ( perm != existing )
        {
            existing = existing.updateFrom( perm );
        }

        return backend.savePermission( existing, autoCommit );
    }

    public Role createRole( final String name, final boolean autoCommit )
        throws UserDataException
    {
        if ( backend.hasRole( name ) )
        {
            throw new UserDataException( "Role already exists: %s", name );
        }

        return backend.saveRole( factory.newRole( name ), autoCommit );
    }

    public Role createRole( final Role role, final boolean autoCommit )
        throws UserDataException
    {
        if ( backend.hasRole( role.getName() ) )
        {
            throw new UserDataException( "Role already exists: %s", role.getName() );
        }

        return backend.saveRole( role, autoCommit );
    }

    public Role updateRole( final Role role, final boolean autoCommit )
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

        return backend.saveRole( existing, autoCommit );
    }

    public User createUser( final User user, final boolean autoCommit )
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

        return backend.saveUser( user, autoCommit );
    }

    public User updateUser( final User user, final boolean autoCommit )
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

        return backend.saveUser( existing, autoCommit );
    }

    @Produces
    @Named
    public List<User> getUsers()
    {
        return backend.getUsers();
    }

    @Produces
    @Named
    public List<Role> getRoles()
    {
        return backend.getRoles();
    }

    @Produces
    @Named
    public List<Permission> getPermissions()
    {
        return backend.getPermissions();
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

    public Map<String, Permission> createCRUDPermissions( final String namespace, final String name,
                                                          final boolean autoCommit )
        throws UserDataException
    {
        try
        {
            if ( autoCommit )
            {
                tx.begin();
            }

            final Map<String, Permission> perms = new HashMap<String, Permission>();
            perms.put( CREATE, factory.newPermission( namespace, name, CREATE ) );
            perms.put( READ, factory.newPermission( namespace, name, READ ) );
            perms.put( UPDATE, factory.newPermission( namespace, name, UPDATE ) );
            perms.put( DELETE, factory.newPermission( namespace, name, DELETE ) );

            for ( final Permission perm : perms.values() )
            {
                backend.savePermission( perm, false );
            }

            if ( autoCommit )
            {
                tx.commit();
            }

            return perms;
        }
        catch ( final NotSupportedException e )
        {
            throw new UserDataException( "Cannot create CRUD permissions for: %s:%s. Error: %s", e, namespace, name,
                                         e.getMessage() );
        }
        catch ( final SystemException e )
        {
            throw new UserDataException( "Cannot create CRUD permissions for: %s:%s. Error: %s", e, namespace, name,
                                         e.getMessage() );
        }
        catch ( final RollbackException e )
        {
            throw new UserDataException( "Cannot create CRUD permissions for: %s:%s. Error: %s", e, namespace, name,
                                         e.getMessage() );
        }
        catch ( final HeuristicMixedException e )
        {
            throw new UserDataException( "Cannot create CRUD permissions for: %s:%s. Error: %s", e, namespace, name,
                                         e.getMessage() );
        }
        catch ( final HeuristicRollbackException e )
        {
            throw new UserDataException( "Cannot create CRUD permissions for: %s:%s. Error: %s", e, namespace, name,
                                         e.getMessage() );
        }
    }

    public void deletePermission( final String name )
        throws UserDataException
    {
        backend.deletePermission( name );
    }

    public void deleteRole( final String name )
        throws UserDataException
    {
        backend.deleteRole( name );
    }

    public void deleteUser( final String username )
        throws UserDataException
    {
        backend.deleteUser( username );
    }

}
