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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Qualifier;
import javax.persistence.EntityManager;
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

import org.commonjava.enterprise.po.MailException;
import org.commonjava.enterprise.po.MailMessage;
import org.commonjava.util.logging.Logger;
import org.commonjava.web.user.model.Permission;
import org.commonjava.web.user.model.Role;
import org.commonjava.web.user.model.User;

@RequestScoped
public class UserDataManager
{
    public static final String CREATE = "create";

    public static final String READ = "read";

    public static final String UPDATE = "update";

    public static final String DELETE = "delete";

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

    // @Inject
    // private PostOffice mailManager;

    @Inject
    private PasswordManager passwordManager;

    public void createUser( final User user, final boolean autoCommit )
        throws MailException, UserDataException
    {
        final String password = passwordManager.generatePassword();

        final MailMessage message = new MailMessage( UserMailTemplates.NEW_USER.template(), user.getEmail() );
        message.property( "user", user );
        message.property( "password", password );

        // mailManager.sendMessage( message );

        logger.info( "Encrypting password." );
        user.setPasswordDigest( passwordManager.digestPassword( password ) );

        saveUser( user, autoCommit );
    }

    @Produces
    @Named
    public List<User> getUsers()
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<User> query = cb.createQuery( User.class );
        final Root<User> root = query.from( User.class );

        query.select( root )
             .orderBy( cb.asc( root.get( "lastName" ) ), cb.asc( root.get( "firstName" ) ) );

        return em.createQuery( query )
                 .getResultList();
    }

    @Produces
    @Named
    public List<Role> getRoles()
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Role> query = cb.createQuery( Role.class );
        final Root<Role> root = query.from( Role.class );

        query.select( root )
             .orderBy( cb.asc( root.get( "name" ) ) );

        return em.createQuery( query )
                 .getResultList();
    }

    @Produces
    @Named
    public List<Permission> getPermissions()
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Permission> query = cb.createQuery( Permission.class );
        final Root<Permission> root = query.from( Permission.class );

        query.select( root )
             .orderBy( cb.asc( root.get( "name" ) ) );

        return em.createQuery( query )
                 .getResultList();
    }

    public void saveUser( final User user, final boolean autoCommit )
        throws UserDataException
    {
        try
        {
            if ( autoCommit )
            {
                tx.begin();
            }

            em.joinTransaction();
            em.persist( user );

            if ( autoCommit )
            {
                tx.commit();
            }
            userEventSrc.fire( user );
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

    public void saveRole( final Role role, final boolean autoCommit )
        throws UserDataException
    {
        try
        {
            if ( autoCommit )
            {
                tx.begin();
            }

            em.joinTransaction();
            em.persist( role );

            if ( autoCommit )
            {
                tx.commit();
            }

            roleEventSrc.fire( role );
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

    public void savePermission( final Permission perm, final boolean autoCommit )
        throws UserDataException
    {
        try
        {
            if ( autoCommit )
            {
                tx.begin();
            }

            em.joinTransaction();
            em.persist( perm );

            if ( autoCommit )
            {
                tx.commit();
            }

            permissionEventSrc.fire( perm );
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

    public User getUser( final String username )
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<User> query = cb.createQuery( User.class );
        final Root<User> root = query.from( User.class );

        query.select( root )
             .where( cb.equal( root.get( "username" ), username ) );

        return em.createQuery( query )
                 .getSingleResult();
    }

    public Permission getPermission( final String permissionName )
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Permission> query = cb.createQuery( Permission.class );
        final Root<Permission> root = query.from( Permission.class );

        query.select( root )
             .where( cb.equal( root.get( "name" ), permissionName ) );

        return em.createQuery( query )
                 .getSingleResult();
    }

    public Role getRole( final String roleName )
    {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<Role> query = cb.createQuery( Role.class );
        final Root<Role> root = query.from( Role.class );

        query.select( root )
             .where( cb.equal( root.get( "name" ), roleName ) );

        return em.createQuery( query )
                 .getSingleResult();
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

            em.joinTransaction();

            final Map<String, Permission> perms = new HashMap<String, Permission>();
            perms.put( CREATE, new Permission( namespace, name, CREATE ) );
            perms.put( READ, new Permission( namespace, name, READ ) );
            perms.put( UPDATE, new Permission( namespace, name, UPDATE ) );
            perms.put( DELETE, new Permission( namespace, name, DELETE ) );

            for ( final Permission perm : perms.values() )
            {
                savePermission( perm, false );
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

}
