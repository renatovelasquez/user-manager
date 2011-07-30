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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.commonjava.util.logging.Logger;
import org.commonjava.web.user.conf.UserManagerConfiguration;
import org.commonjava.web.user.model.Permission;
import org.commonjava.web.user.model.Role;
import org.commonjava.web.user.model.User;

@Singleton
public class UserDataInitializer
{
    private final Logger logger = new Logger( getClass() );

    @Inject
    private UserDataContext ctx;

    @Inject
    private UserDataManager dataManager;

    @Inject
    private UserManagerConfiguration userManagerConfig;

    @Inject
    private PasswordManager passwordManager;

    public void initializeAdmin()
        throws UserDataException
    {
        // try
        // {
        ctx.begin();

        Permission perm = dataManager.getPermission( Permission.WILDCARD );
        if ( perm == null )
        {
            perm = new Permission( Permission.WILDCARD );
            logger.info( "Creating wildcard permission: %s", perm );

            dataManager.createPermission( perm, false );
        }

        Role role = dataManager.getRole( Role.ADMIN );
        if ( role == null )
        {
            role = new Role( Role.ADMIN, perm );
            role.addPermission( perm );
            logger.info( "Creating admin role: %s", role );

            dataManager.createRole( role, false );
        }

        User user = dataManager.getUser( User.ADMIN );
        if ( user == null )
        {
            user = userManagerConfig.createInitialAdminUser( passwordManager );
            user.addRole( role );
            logger.info( "Creating admin user: %s", user );

            dataManager.createUser( user, false );
        }

        ctx.commit();
        ctx.sendNotifications();
        // }
        // catch ( NotSupportedException e )
        // {
        // throw new UserDataException( "Failed to initialize admin user/role/permissions: %s", e,
        // e.getMessage() );
        // }
        // catch ( SystemException e )
        // {
        // throw new UserDataException( "Failed to initialize admin user/role/permissions: %s", e,
        // e.getMessage() );
        // }
        // catch ( RollbackException e )
        // {
        // throw new UserDataException( "Failed to initialize admin user/role/permissions: %s", e,
        // e.getMessage() );
        // }
        // catch ( HeuristicMixedException e )
        // {
        // throw new UserDataException( "Failed to initialize admin user/role/permissions: %s", e,
        // e.getMessage() );
        // }
        // catch ( HeuristicRollbackException e )
        // {
        // throw new UserDataException( "Failed to initialize admin user/role/permissions: %s", e,
        // e.getMessage() );
        // }
    }

}
