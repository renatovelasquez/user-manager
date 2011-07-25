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
package org.commonjava.web.user.controller;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.commonjava.util.logging.Logger;
import org.commonjava.web.user.data.UserDataException;
import org.commonjava.web.user.data.UserDataManager;
import org.commonjava.web.user.model.User;

@Model
public class UserAdmin
{

    private final Logger logger = new Logger( getClass() );

    @Inject
    private UserDataManager dataManager;

    private User newUser;

    @Produces
    @Named
    public User getNewUser()
    {
        return newUser;
    }

    public void createUser()
        throws UserDataException
    {
        logger.info( "\n\nSaving user: %s\n\n", newUser );

        logger.info( "Generating password." );

        dataManager.createUser( newUser, true );
        createNewUserInstance();
    }

    public void generateUsername()
    {
        if ( newUser.getLastName() != null && newUser.getFirstName() != null )
        {
            final StringBuilder sb = new StringBuilder();

            sb.append( Character.toLowerCase( newUser.getFirstName()
                                                     .charAt( 0 ) ) );
            sb.append( Character.toLowerCase( newUser.getLastName()
                                                     .charAt( 0 ) ) );

            if ( newUser.getLastName()
                        .length() > 1 )
            {
                sb.append( newUser.getLastName()
                                  .substring( 1 ) );
            }

            newUser.setUsername( sb.toString() );
        }
    }

    @PostConstruct
    private void createNewUserInstance()
    {
        newUser = new User( User.NOT_SPECIFIED );
    }

}
