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
package org.commonjava.web.user.seed;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.commonjava.util.logging.Logger;
import org.commonjava.web.user.conf.UserManagerConfiguration;
import org.commonjava.web.user.data.PasswordManager;
import org.commonjava.web.user.data.UserDataManager;
import org.commonjava.web.user.model.Permission;
import org.commonjava.web.user.model.Role;
import org.commonjava.web.user.model.User;

@WebListener
public class UserManagerInitializer
    implements ServletContextListener
{
    private final Logger logger = new Logger( getClass() );

    @Inject
    private UserDataManager dataManager;

    @Inject
    private UserManagerConfiguration userManagerConfig;

    @Inject
    private PasswordManager passwordManager;

    @Override
    public void contextInitialized( final ServletContextEvent sce )
    {
        try
        {
            Permission perm = dataManager.getPermission( Permission.WILDCARD );
            if ( perm == null )
            {
                perm = new Permission( Permission.WILDCARD );
                logger.info( "Creating wildcard permission: %s", perm );

                dataManager.savePermission( perm, true );
            }

            Role role = dataManager.getRole( Role.ADMIN );
            if ( role == null )
            {
                role = new Role( Role.ADMIN, perm );
                role.addPermission( perm );
                logger.info( "Creating admin role: %s", role );

                dataManager.saveRole( role, true );
            }

            User user = dataManager.getUser( User.ADMIN );
            if ( user == null )
            {
                user = userManagerConfig.getInitialAdminUser( passwordManager );
                user.addRole( role );
                logger.info( "Creating admin user: %s", user );

                dataManager.saveUser( user, true );
            }
        }
        catch ( final Exception e )
        {
            logger.error( "Seed data import failed: %s", e, e.getMessage() );
        }
    }

    @Override
    public void contextDestroyed( final ServletContextEvent sce )
    {
    }
}
