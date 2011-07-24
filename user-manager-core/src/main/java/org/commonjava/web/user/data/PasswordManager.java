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

import java.security.SecureRandom;

import javax.inject.Singleton;

import org.apache.shiro.crypto.hash.Sha512Hash;

@Singleton
public class PasswordManager
{

    private static final String ONETIME_PASSWORD_SEED = "23456789abcdefghkmnpqrstuvwxyzABCDEFGHKMNPQRSTUVWXYZ-_!.";

    private static final int ONETIME_PASSWORD_LENGTH = 15;

    private final SecureRandom randomGenerator = new SecureRandom();

    public String generatePassword()
    {
        final StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < ONETIME_PASSWORD_LENGTH; i++ )
        {
            final int idx = Math.abs( randomGenerator.nextInt() ) % ONETIME_PASSWORD_SEED.length();
            sb.append( ONETIME_PASSWORD_SEED.charAt( idx ) );
        }

        return sb.toString();
    }

    public boolean verifyPassword( final String digest, final String password )
    {
        return digest.equals( new Sha512Hash( password ).toHex() );
    }

    public String digestPassword( final String password )
    {
        return new Sha512Hash( password ).toHex();
    }

}
