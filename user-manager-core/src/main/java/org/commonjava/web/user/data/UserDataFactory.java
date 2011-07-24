package org.commonjava.web.user.data;

import org.commonjava.web.user.model.Permission;
import org.commonjava.web.user.model.Role;
import org.commonjava.web.user.model.User;

public interface UserDataFactory<U extends User, R extends Role, P extends Permission>
{

    U newUser( String username );

    R newRole( String name, Permission... permissions );

    P newPermission( String... nameParts );

    U toNative( User user );

    R toNative( Role role );

    P toNative( Permission permission );

}
