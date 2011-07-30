package org.commonjava.web.user.data;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.commonjava.web.user.model.Permission;
import org.commonjava.web.user.model.Role;
import org.commonjava.web.user.model.User;

public class UserDataContext
{

    // private final Logger logger = new Logger( getClass() );

    @Inject
    private Event<User> userEventSrc;

    @Inject
    private Event<Role> roleEventSrc;

    @Inject
    private Event<Permission> permissionEventSrc;

    private final Set<User> changedUsers = new HashSet<User>();

    private final Set<Role> changedRoles = new HashSet<Role>();

    private final Set<Permission> changedPermissions = new HashSet<Permission>();

    @Inject
    private UserTransaction tx;

    private boolean inTx = false;

    private boolean outTx = false;

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
            throw new UserDataException(
                                         "Transaction cannot be rolled back if it has not been started! Call begin() first!" );
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

            outTx = true;
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
            userEventSrc.fire( changedUsers.iterator().next() );
        }

        if ( !changedRoles.isEmpty() )
        {
            roleEventSrc.fire( changedRoles.iterator().next() );
        }

        if ( !changedPermissions.isEmpty() )
        {
            permissionEventSrc.fire( changedPermissions.iterator().next() );
        }

    }

}
