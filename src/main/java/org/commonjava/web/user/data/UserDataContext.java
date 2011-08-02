package org.commonjava.web.user.data;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.persistence.EntityManager;
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

public class UserDataContext
{

    private final Logger logger = new Logger( getClass() );

    private final Event<User> userEventSrc;

    private final Event<Role> roleEventSrc;

    private final Event<Permission> permissionEventSrc;

    private final UserTransaction tx;

    private final EntityManager em;

    private final Set<User> changedUsers = new HashSet<User>();

    private final Set<Role> changedRoles = new HashSet<Role>();

    private final Set<Permission> changedPermissions = new HashSet<Permission>();

    private boolean inTx = false;

    private boolean outTx = false;

    UserDataContext( final Event<User> userEventSrc, final Event<Role> roleEventSrc,
                     final Event<Permission> permissionEventSrc, final UserTransaction tx,
                     final EntityManager em )
    {
        this.userEventSrc = userEventSrc;
        this.roleEventSrc = roleEventSrc;
        this.permissionEventSrc = permissionEventSrc;
        this.tx = tx;
        this.em = em;
    }

    public synchronized UserDataContext begin()
        throws UserDataException
    {
        if ( outTx )
        {
            throw new UserDataException( "Transaction already closed for this data context!" );
        }

        if ( !inTx )
        {
            try
            {
                tx.begin();
                em.joinTransaction();
            }
            catch ( NotSupportedException e )
            {
                throw new UserDataException( "Failed to begin transaction: %s", e, e.getMessage() );
            }
            catch ( SystemException e )
            {
                throw new UserDataException( "Failed to begin transaction: %s", e, e.getMessage() );
            }

            inTx = true;
        }

        return this;
    }

    public synchronized UserDataContext commit()
        throws UserDataException
    {
        if ( !inTx )
        {
            throw new UserDataException(
                                         "Transaction cannot be committed until it has been started! Call begin() first!" );
        }

        if ( !outTx )
        {
            try
            {
                tx.commit();
            }
            catch ( RollbackException e )
            {
                throw new UserDataException( "Failed to commit transaction: %s", e, e.getMessage() );
            }
            catch ( HeuristicMixedException e )
            {
                throw new UserDataException( "Failed to commit transaction: %s", e, e.getMessage() );
            }
            catch ( HeuristicRollbackException e )
            {
                throw new UserDataException( "Failed to commit transaction: %s", e, e.getMessage() );
            }
            catch ( SystemException e )
            {
                throw new UserDataException( "Failed to commit transaction: %s", e, e.getMessage() );
            }

            outTx = true;
        }

        return this;
    }

    public synchronized UserDataContext rollback()
        throws UserDataException
    {
        if ( !inTx )
        {
            logger.warn( "Transaction cannot be rolled back if it has not been started! Call begin() first! Skipping..." );
        }

        if ( !outTx )
        {
            try
            {
                tx.rollback();
            }
            catch ( SystemException e )
            {
                throw new UserDataException( "Failed to rollback transaction: %s", e,
                                             e.getMessage() );
            }
            finally
            {
                outTx = true;
            }
        }

        return this;
    }

    public UserDataContext userChanged( final User user )
    {
        changedUsers.add( user );
        return this;
    }

    public UserDataContext roleChanged( final Role role )
    {
        changedRoles.add( role );
        return this;
    }

    public UserDataContext permissionChanged( final Permission perm )
    {
        changedPermissions.add( perm );
        return this;
    }

    public void sendNotifications()
    {
        if ( !changedUsers.isEmpty() )
        {
            logger.info( "\n\n\n\nSending user change notifications..." );
            userEventSrc.fire( changedUsers.iterator().next() );
        }

        if ( !changedRoles.isEmpty() )
        {
            logger.info( "\n\n\n\nSending role change notifications..." );
            roleEventSrc.fire( changedRoles.iterator().next() );
        }

        if ( !changedPermissions.isEmpty() )
        {
            logger.info( "\n\n\n\nSending permission change notifications..." );
            permissionEventSrc.fire( changedPermissions.iterator().next() );
        }

    }

}
