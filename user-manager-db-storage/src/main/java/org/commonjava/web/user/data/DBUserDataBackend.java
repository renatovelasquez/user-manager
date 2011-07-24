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
import java.util.List;

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
import org.commonjava.web.user.model.DBPermission;
import org.commonjava.web.user.model.DBRole;
import org.commonjava.web.user.model.DBUser;
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
    private Event<User> userEventSrc;

    @Inject
    private Event<Role> roleEventSrc;

    @Inject
    private Event<Permission> permissionEventSrc;

    @Inject
    private UserTransaction tx;

    @Inject
    private DBUserDataFactory factory;

    @Override
    public boolean hasUser( final String username )
    {
        return em.contains( new DBUser( username ) );
    }

    @Override
    public boolean hasRole( final String name )
    {
        return em.contains( new DBRole( name ) );
    }

    @Override
    public boolean hasPermission( final String name )
    {
        return em.contains( new DBPermission( name ) );
    }

    @Override
    public List<User> getUsers()
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<DBUser> query = cb.createQuery( DBUser.class );
        final Root<DBUser> root = query.from( DBUser.class );

        query.select( root )
             .orderBy( cb.asc( root.get( "lastName" ) ), cb.asc( root.get( "firstName" ) ) );

        return generalizeUsers( em.createQuery( query )
                                  .getResultList() );
    }

    @Override
    public List<Role> getRoles()
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<DBRole> query = cb.createQuery( DBRole.class );
        final Root<DBRole> root = query.from( DBRole.class );

        query.select( root )
             .orderBy( cb.asc( root.get( "name" ) ) );

        return generalizeRoles( em.createQuery( query )
                                  .getResultList() );
    }

    @Override
    public List<Permission> getPermissions()
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<DBPermission> query = cb.createQuery( DBPermission.class );
        final Root<DBPermission> root = query.from( DBPermission.class );

        query.select( root )
             .orderBy( cb.asc( root.get( "name" ) ) );

        return generalizePermissions( em.createQuery( query )
                                        .getResultList() );
    }

    @Override
    public User saveUser( final User user, final boolean autoCommit )
        throws UserDataException
    {
        try
        {
            if ( autoCommit )
            {
                tx.begin();
            }

            final DBUser dbUser = factory.toNative( user );
            em.joinTransaction();

            boolean success = true;
            try
            {
                em.persist( dbUser );
            }
            catch ( final EntityExistsException e )
            {
                success = false;
                logger.error( "\n\n\nUser exists: %s\n\n\n", e, user.getUsername() );
            }

            if ( autoCommit )
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

            userEventSrc.fire( dbUser );

            return dbUser;
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
    public Role saveRole( final Role role, final boolean autoCommit )
        throws UserDataException
    {
        try
        {
            if ( autoCommit )
            {
                tx.begin();
            }

            em.joinTransaction();

            final DBRole dbRole = factory.toNative( role );

            boolean success = true;
            try
            {
                em.persist( dbRole );
            }
            catch ( final EntityExistsException e )
            {
                success = false;
                logger.error( "\n\n\nRole exists: %s\n\n\n", e, role.getName() );
            }

            if ( autoCommit )
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

            roleEventSrc.fire( dbRole );

            return dbRole;
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
    public Permission savePermission( final Permission perm, final boolean autoCommit )
        throws UserDataException
    {
        try
        {
            if ( autoCommit )
            {
                tx.begin();
            }

            em.joinTransaction();

            final DBPermission dbPerm = factory.toNative( perm );

            boolean success = true;
            try
            {
                em.persist( dbPerm );
            }
            catch ( final EntityExistsException e )
            {
                success = false;
                logger.error( "\n\n\nPermission exists: %s\n\n\n", e, perm.getName() );
            }

            if ( autoCommit )
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

            permissionEventSrc.fire( dbPerm );

            return dbPerm;
        }
        catch ( final NotSupportedException e )
        {
            throw new UserDataException( "Cannot save permission: %s. Error: %s", e, perm, e.getMessage() );
        }
        catch ( final SystemException e )
        {
            throw new UserDataException( "Cannot save permission: %s. Error: %s", e, perm, e.getMessage() );
        }
        catch ( final RollbackException e )
        {
            throw new UserDataException( "Cannot save permission: %s. Error: %s", e, perm, e.getMessage() );
        }
        catch ( final HeuristicMixedException e )
        {
            throw new UserDataException( "Cannot save permission: %s. Error: %s", e, perm, e.getMessage() );
        }
        catch ( final HeuristicRollbackException e )
        {
            throw new UserDataException( "Cannot save permission: %s. Error: %s", e, perm, e.getMessage() );
        }
    }

    @Override
    public DBUser getUser( final String username )
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<DBUser> query = cb.createQuery( DBUser.class );
        final Root<DBUser> root = query.from( DBUser.class );

        query.select( root )
             .where( cb.equal( root.get( "username" ), username ) );

        DBUser user = null;
        try
        {
            // TODO: cleaner way to check for user existence...
            user = em.createQuery( query )
                     .getSingleResult();
        }
        catch ( final NoResultException e )
        {
            logger.debug( "Cannot find user: %s. Error: %s", e, username, e.getMessage() );
        }

        return user;
    }

    @Override
    public DBPermission getPermission( final String permissionName )
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<DBPermission> query = cb.createQuery( DBPermission.class );
        final Root<DBPermission> root = query.from( DBPermission.class );

        query.select( root )
             .where( cb.equal( root.get( "name" ), permissionName ) );

        DBPermission perm = null;
        try
        {
            // TODO: cleaner way to check for user existence...
            perm = em.createQuery( query )
                     .getSingleResult();
        }
        catch ( final NoResultException e )
        {
            logger.debug( "Cannot find permission: %s. Error: %s", e, permissionName, e.getMessage() );
        }

        return perm;
    }

    @Override
    public DBRole getRole( final String roleName )
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<DBRole> query = cb.createQuery( DBRole.class );
        final Root<DBRole> root = query.from( DBRole.class );

        query.select( root )
             .where( cb.equal( root.get( "name" ), roleName ) );

        DBRole role = null;

        try
        {
            // TODO: cleaner way to check for user existence...
            role = em.createQuery( query )
                     .getSingleResult();
        }
        catch ( final NoResultException e )
        {
            logger.debug( "Cannot find role: %s. Error: %s", e, roleName, e.getMessage() );
        }

        return role;
    }

    // public void onUserChanged( @Observes( notifyObserver = Reception.IF_EXISTS ) final User user )
    // {
    // loadData();
    // }
    //
    // @PostConstruct
    // public void loadData()
    // {
    // }

    @Override
    public void deletePermission( final String name )
        throws UserDataException
    {
        try
        {
            final Permission perm = getPermission( name );
            if ( perm == null )
            {
                throw new UserDataException( "No such permission: %s", name );
            }

            em.remove( perm );
        }
        catch ( final IllegalArgumentException e )
        {
            logger.debug( "Cannot remove permission: %s. Error: %s", e, name, e.getMessage() );
            throw new UserDataException( "Cannot delete permission: %s. Error: %s", e, name, e.getMessage() );
        }
    }

    @Override
    public void deleteRole( final String name )
        throws UserDataException
    {
        try
        {
            final Role role = getRole( name );
            if ( role == null )
            {
                throw new UserDataException( "No such role: %s", name );
            }

            em.remove( role );
        }
        catch ( final IllegalArgumentException e )
        {
            logger.debug( "Cannot remove role: %s. Error: %s", e, name, e.getMessage() );
            throw new UserDataException( "Cannot delete role: %s. Error: %s", e, name, e.getMessage() );
        }
    }

    @Override
    public void deleteUser( final String username )
        throws UserDataException
    {
        try
        {
            final User user = getUser( username );
            if ( user == null )
            {
                throw new UserDataException( "No such user: %s", username );
            }

            em.remove( user );
        }
        catch ( final IllegalArgumentException e )
        {
            logger.debug( "Cannot remove user: %s. Error: %s", e, username, e.getMessage() );
            throw new UserDataException( "Cannot delete user: %s. Error: %s", e, username, e.getMessage() );
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
    {
    }

}
