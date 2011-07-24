package org.commonjava.web.user.data;

import javax.inject.Singleton;

import org.commonjava.web.user.model.DBPermission;
import org.commonjava.web.user.model.DBRole;
import org.commonjava.web.user.model.DBUser;
import org.commonjava.web.user.model.Permission;
import org.commonjava.web.user.model.Role;
import org.commonjava.web.user.model.User;

@Singleton
public class DBUserDataFactory
    implements UserDataFactory<DBUser, DBRole, DBPermission>
{

    @Override
    public DBUser newUser( final String username )
    {
        return new DBUser( username );
    }

    @Override
    public DBRole newRole( final String name, final Permission... permissions )
    {
        return new DBRole( name, permissions );
    }

    @Override
    public DBPermission newPermission( final String... nameParts )
    {
        return new DBPermission( nameParts );
    }

    @Override
    public DBUser toNative( final User user )
    {
        return ( user instanceof DBUser ) ? (DBUser) user : new DBUser( user.getUsername() ).updateFrom( user );
    }

    @Override
    public DBRole toNative( final Role role )
    {
        return ( role instanceof DBRole ) ? (DBRole) role : new DBRole( role.getName() ).updateFrom( role );
    }

    @Override
    public DBPermission toNative( final Permission permission )
    {
        return ( permission instanceof DBPermission ) ? (DBPermission) permission
                        : new DBPermission( permission.getName() ).updateFrom( permission );
    }

}
