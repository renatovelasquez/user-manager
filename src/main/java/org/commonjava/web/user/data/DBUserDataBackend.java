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

import static org.commonjava.web.user.model.GeneralizationUtils.generalizePermissions;
import static org.commonjava.web.user.model.GeneralizationUtils.generalizeRoles;
import static org.commonjava.web.user.model.GeneralizationUtils.generalizeUsers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
public class DBUserDataBackend
    implements UserDataBackend
{
    private final Logger logger = new Logger( getClass() );

    @Inject
    @UserRepository
    private EntityManager em;

    @Inject
    private Event<Set<User>> userEventSrc;

    @Inject
    private Event<Set<Role>> roleEventSrc;

    @Inject
    private Event<Set<Permission>> permissionEventSrc;

    @Inject
    private UserTransaction tx;

    @Override
    public boolean hasUser( final String username )
    {
        return em.contains( new User( username ) );
    }

    @Override
    public boolean hasRole( final String name )
    {
        return em.contains( new Role( name ) );
    }

    @Override
    public boolean hasPermission( final String name )
    {
        return em.contains( new Permission( name ) );
    }

    @Override
    public List<User> getUsers()
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<User> query = cb.createQuery( User.class );
        final Root<User> root = query.from( User.class );

        query.select( root ).orderBy( cb.asc( root.get( "lastName" ) ),
                                      cb.asc( root.get( "firstName" ) ) );

        return generalizeUsers( em.createQuery( query ).getResultList() );
    }

    @Override
    public List<Role> getRoles()
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Role> query = cb.createQuery( Role.class );
        final Root<Role> root = query.from( Role.class );

        query.select( root ).orderBy( cb.asc( root.get( "name" ) ) );

        return generalizeRoles( em.createQuery( query ).getResultList() );
    }

    @Override
    public List<Permission> getPermissions()
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Permission> query = cb.createQuery( Permission.class );
        final Root<Permission> root = query.from( Permission.class );

        query.select( root ).orderBy( cb.asc( root.get( "name" ) ) );

        return generalizePermissions( em.createQuery( query ).getResultList() );
    }

    @Override
    public User saveUser( final User user )
        throws UserDataException
    {
        return saveUser( user, null );
    }

    @Override
    public User saveUser( final User user, final UserNotificationContext notificationContext )
        throws UserDataException
    {
        try
        {
            if ( notificationContext == null )
            {
                tx.begin();
            }

            em.joinTransaction();

            boolean success = true;
            try
            {
                em.persist( user );
            }
            catch ( final EntityExistsException e )
            {
                success = false;
                logger.error( "\n\n\nUser exists: %s\n\n\n", e, user.getUsername() );
            }

            if ( notificationContext == null )
            {
                if ( success )
                {
                    tx.commit();
                }
                else
                {
                    tx.rollback();
                }
            }

            if ( notificationContext != null )
            {
                notificationContext.userChanged( user );
            }
            else
            {
                userEventSrc.fire( Collections.singleton( user ) );
            }

            return user;
        }
        catch ( final NotSupportedException e )
        {
            throw new UserDataException( "Cannot save user: %s. Error: %s", e, user, e.getMessage() );
        }
        catch ( final SystemException e )
        {
            throw new UserDataException( "Cannot save user: %s. Error: %s", e, user, e.getMessage() );
        }
        catch ( final RollbackException e )
        {
            throw new UserDataException( "Cannot save user: %s. Error: %s", e, user, e.getMessage() );
        }
        catch ( final HeuristicMixedException e )
        {
            throw new UserDataException( "Cannot save user: %s. Error: %s", e, user, e.getMessage() );
        }
        catch ( final HeuristicRollbackException e )
        {
            throw new UserDataException( "Cannot save user: %s. Error: %s", e, user, e.getMessage() );
        }
    }

    @Override
    public Role saveRole( final Role role )
        throws UserDataException
    {
        return saveRole( role, null );
    }

    @Override
    public Role saveRole( final Role role, final UserNotificationContext notificationContext )
        throws UserDataException
    {
        try
        {
            if ( notificationContext == null )
            {
                tx.begin();
            }

            em.joinTransaction();

            boolean success = true;
            try
            {
                em.persist( role );
            }
            catch ( final EntityExistsException e )
            {
                success = false;
                logger.error( "\n\n\nRole exists: %s\n\n\n", e, role.getName() );
            }

            if ( notificationContext == null )
            {
                if ( success )
                {
                    tx.commit();
                }
                else
                {
                    tx.rollback();
                }
            }

            if ( notificationContext != null )
            {
                notificationContext.roleChanged( role );
            }
            else
            {
                roleEventSrc.fire( Collections.singleton( role ) );
            }

            return role;
        }
        catch ( final NotSupportedException e )
        {
            throw new UserDataException( "Cannot save role: %s. Error: %s", e, role, e.getMessage() );
        }
        catch ( final SystemException e )
        {
            throw new UserDataException( "Cannot save role: %s. Error: %s", e, role, e.getMessage() );
        }
        catch ( final RollbackException e )
        {
            throw new UserDataException( "Cannot save role: %s. Error: %s", e, role, e.getMessage() );
        }
        catch ( final HeuristicMixedException e )
        {
            throw new UserDataException( "Cannot save role: %s. Error: %s", e, role, e.getMessage() );
        }
        catch ( final HeuristicRollbackException e )
        {
            throw new UserDataException( "Cannot save role: %s. Error: %s", e, role, e.getMessage() );
        }
    }

    @Override
    public Permission savePermission( final Permission perm )
        throws UserDataException
    {
        return savePermission( perm, null );
    }

    @Override
    public Permission savePermission( final Permission perm,
                                      final UserNotificationContext notificationContext )
        throws UserDataException
    {
        try
        {
            if ( notificationContext == null )
            {
                tx.begin();
            }

            em.joinTransaction();

            boolean success = true;
            try
            {
                em.persist( perm );
            }
            catch ( final EntityExistsException e )
            {
                success = false;
                logger.error( "\n\n\nPermission exists: %s\n\n\n", e, perm.getName() );
            }

            if ( notificationContext == null )
            {
                if ( success )
                {
                    tx.commit();
                }
                else
                {
                    tx.rollback();
                }
            }

            if ( notificationContext != null )
            {
                notificationContext.permissionChanged( perm );
            }
            else
            {
                permissionEventSrc.fire( Collections.singleton( perm ) );
            }

            return perm;
        }
        catch ( final NotSupportedException e )
        {
            throw new UserDataException( "Cannot save permission: %s. Error: %s", e, perm,
                                         e.getMessage() );
        }
        catch ( final SystemException e )
        {
            throw new UserDataException( "Cannot save permission: %s. Error: %s", e, perm,
                                         e.getMessage() );
        }
        catch ( final RollbackException e )
        {
            throw new UserDataException( "Cannot save permission: %s. Error: %s", e, perm,
                                         e.getMessage() );
        }
        catch ( final HeuristicMixedException e )
        {
            throw new UserDataException( "Cannot save permission: %s. Error: %s", e, perm,
                                         e.getMessage() );
        }
        catch ( final HeuristicRollbackException e )
        {
            throw new UserDataException( "Cannot save permission: %s. Error: %s", e, perm,
                                         e.getMessage() );
        }
    }

    @Override
    public User getUser( final String username )
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<User> query = cb.createQuery( User.class );
        final Root<User> root = query.from( User.class );

        query.select( root ).where( cb.equal( root.get( "username" ), username ) );

        User user = null;
        try
        {
            // TODO: cleaner way to check for user existence...
            user = em.createQuery( query ).getSingleResult();
        }
        catch ( final NoResultException e )
        {
            logger.debug( "Cannot find user: %s. Error: %s", e, username, e.getMessage() );
        }

        return user;
    }

    @Override
    public Permission getPermission( final String permissionName )
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Permission> query = cb.createQuery( Permission.class );
        final Root<Permission> root = query.from( Permission.class );

        query.select( root ).where( cb.equal( root.get( "name" ), permissionName ) );

        Permission perm = null;
        try
        {
            // TODO: cleaner way to check for user existence...
            perm = em.createQuery( query ).getSingleResult();
        }
        catch ( final NoResultException e )
        {
            logger.debug( "Cannot find permission: %s. Error: %s", e, permissionName,
                          e.getMessage() );
        }

        return perm;
    }

    @Override
    public Role getRole( final String roleName )
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Role> query = cb.createQuery( Role.class );
        final Root<Role> root = query.from( Role.class );

        query.select( root ).where( cb.equal( root.get( "name" ), roleName ) );

        Role role = null;

        try
        {
            // TODO: cleaner way to check for user existence...
            role = em.createQuery( query ).getSingleResult();
        }
        catch ( final NoResultException e )
        {
            logger.debug( "Cannot find role: %s. Error: %s", e, roleName, e.getMessage() );
        }

        return role;
    }

    @Override
    public void deletePermission( final String name )
        throws UserDataException
    {
        deletePermission( name, null );
    }

    @Override
    public void deletePermission( final String name,
                                  final UserNotificationContext notificationContext )
        throws UserDataException
    {
        try
        {
            if ( notificationContext == null )
            {
                logger.info( "Starting transaction: %s", tx );
                tx.begin();
            }

            logger.info( "Joining transaction: %s for entity manager: %s", tx, em );
            em.joinTransaction();

            boolean success = true;
            final Permission perm = getPermission( name );
            if ( perm == null )
            {
                throw new UserDataException( "No such permission: %s", name );
            }

            em.remove( perm );

            if ( notificationContext == null )
            {
                if ( success )
                {
                    logger.info( "Committing transaction: %s", tx );
                    tx.commit();
                }
                else
                {
                    tx.rollback();
                }
            }

            if ( notificationContext != null )
            {
                notificationContext.permissionChanged( perm );
            }
            else
            {
                permissionEventSrc.fire( Collections.singleton( perm ) );
            }
        }
        catch ( final IllegalArgumentException e )
        {
            logger.debug( "Cannot remove permission: %s. Error: %s", e, name, e.getMessage() );
            throw new UserDataException( "Cannot delete permission: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( RollbackException e )
        {
            logger.debug( "Cannot remove permission: %s. Error: %s", e, name, e.getMessage() );
            throw new UserDataException( "Cannot delete permission: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( HeuristicMixedException e )
        {
            logger.debug( "Cannot remove permission: %s. Error: %s", e, name, e.getMessage() );
            throw new UserDataException( "Cannot delete permission: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( HeuristicRollbackException e )
        {
            logger.debug( "Cannot remove permission: %s. Error: %s", e, name, e.getMessage() );
            throw new UserDataException( "Cannot delete permission: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( SystemException e )
        {
            logger.debug( "Cannot remove permission: %s. Error: %s", e, name, e.getMessage() );
            throw new UserDataException( "Cannot delete permission: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( NotSupportedException e )
        {
            logger.debug( "Cannot remove permission: %s. Error: %s", e, name, e.getMessage() );
            throw new UserDataException( "Cannot delete permission: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
    }

    @Override
    public void deleteRole( final String name )
        throws UserDataException
    {
        deleteRole( name, null );
    }

    @Override
    public void deleteRole( final String name, final UserNotificationContext notificationContext )
        throws UserDataException
    {
        try
        {
            if ( notificationContext == null )
            {
                tx.begin();
            }

            em.joinTransaction();

            boolean success = true;
            final Role role = getRole( name );
            if ( role == null )
            {
                throw new UserDataException( "No such role: %s", name );
            }

            em.remove( role );

            if ( notificationContext == null )
            {
                if ( success )
                {
                    tx.commit();
                }
                else
                {
                    tx.rollback();
                }
            }

            if ( notificationContext != null )
            {
                notificationContext.roleChanged( role );
            }
            else
            {
                roleEventSrc.fire( Collections.singleton( role ) );
            }
        }
        catch ( final IllegalArgumentException e )
        {
            logger.debug( "Cannot remove role: %s. Error: %s", e, name, e.getMessage() );
            throw new UserDataException( "Cannot delete role: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( RollbackException e )
        {
            logger.debug( "Cannot remove role: %s. Error: %s", e, name, e.getMessage() );
            throw new UserDataException( "Cannot delete role: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( HeuristicMixedException e )
        {
            logger.debug( "Cannot remove role: %s. Error: %s", e, name, e.getMessage() );
            throw new UserDataException( "Cannot delete role: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( HeuristicRollbackException e )
        {
            logger.debug( "Cannot remove role: %s. Error: %s", e, name, e.getMessage() );
            throw new UserDataException( "Cannot delete role: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( SystemException e )
        {
            logger.debug( "Cannot remove role: %s. Error: %s", e, name, e.getMessage() );
            throw new UserDataException( "Cannot delete role: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
        catch ( NotSupportedException e )
        {
            logger.debug( "Cannot remove role: %s. Error: %s", e, name, e.getMessage() );
            throw new UserDataException( "Cannot delete role: %s. Error: %s", e, name,
                                         e.getMessage() );
        }
    }

    @Override
    public void deleteUser( final String username )
        throws UserDataException
    {
        deleteUser( username, null );
    }

    @Override
    public void deleteUser( final String username, final UserNotificationContext notificationContext )
        throws UserDataException
    {
        try
        {
            if ( notificationContext == null )
            {
                tx.begin();
            }

            em.joinTransaction();

            boolean success = true;
            final User user = getUser( username );
            if ( user == null )
            {
                throw new UserDataException( "No such user: %s", username );
            }

            em.remove( user );

            if ( notificationContext == null )
            {
                if ( success )
                {
                    tx.commit();
                }
                else
                {
                    tx.rollback();
                }
            }

            if ( notificationContext != null )
            {
                notificationContext.userChanged( user );
            }
            else
            {
                userEventSrc.fire( Collections.singleton( user ) );
            }
        }
        catch ( final IllegalArgumentException e )
        {
            logger.debug( "Cannot remove user: %s. Error: %s", e, username, e.getMessage() );
            throw new UserDataException( "Cannot delete user: %s. Error: %s", e, username,
                                         e.getMessage() );
        }
        catch ( RollbackException e )
        {
            logger.debug( "Cannot remove user: %s. Error: %s", e, username, e.getMessage() );
            throw new UserDataException( "Cannot delete user: %s. Error: %s", e, username,
                                         e.getMessage() );
        }
        catch ( HeuristicMixedException e )
        {
            logger.debug( "Cannot remove user: %s. Error: %s", e, username, e.getMessage() );
            throw new UserDataException( "Cannot delete user: %s. Error: %s", e, username,
                                         e.getMessage() );
        }
        catch ( HeuristicRollbackException e )
        {
            logger.debug( "Cannot remove user: %s. Error: %s", e, username, e.getMessage() );
            throw new UserDataException( "Cannot delete user: %s. Error: %s", e, username,
                                         e.getMessage() );
        }
        catch ( SystemException e )
        {
            logger.debug( "Cannot remove user: %s. Error: %s", e, username, e.getMessage() );
            throw new UserDataException( "Cannot delete user: %s. Error: %s", e, username,
                                         e.getMessage() );
        }
        catch ( NotSupportedException e )
        {
            logger.debug( "Cannot remove user: %s. Error: %s", e, username, e.getMessage() );
            throw new UserDataException( "Cannot delete user: %s. Error: %s", e, username,
                                         e.getMessage() );
        }
    }

    public static final class RepositoryProducer
    {
        @SuppressWarnings( "unused" )
        @Produces
        @UserRepository
        @PersistenceContext
        private EntityManager entityManager;
    }

    @Qualifier
    @Target( { ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD } )
    @Retention( RetentionPolicy.RUNTIME )
    public @interface UserRepository
    {}

    @Override
    public UserNotificationContext createNotificationContext()
    {
        return new UserNotificationContext( userEventSrc, roleEventSrc, permissionEventSrc );
    }

}
