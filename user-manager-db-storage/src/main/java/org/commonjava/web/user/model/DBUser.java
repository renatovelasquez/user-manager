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
package org.commonjava.web.user.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table( name = "users" )
public class DBUser
    implements User
{

    private static final String DEFAULT_REALM = "default";

    @Id
    @GeneratedValue
    private int id;

    private final String realm = DEFAULT_REALM;

    @Length( min = 4, max = 15 )
    @NotBlank
    @Column( unique = true )
    private String username;

    @NotBlank
    private String passwordDigest;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    @ManyToMany
    private Set<Role> roles;

    public DBUser()
    {
    }

    public DBUser( final String username, final String email, final String firstName, final String lastName,
                   final String passwordDigest )
    {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordDigest = passwordDigest;
    }

    public DBUser( final String username )
    {
        this.username = username;
    }

    @Override
    public String getUsername()
    {
        return username;
    }

    @Override
    public void setUsername( final String username )
    {
        this.username = username;
    }

    @Override
    public String getPasswordDigest()
    {
        return passwordDigest;
    }

    @Override
    public void setPasswordDigest( final String passwordDigest )
    {
        this.passwordDigest = passwordDigest;
    }

    @Override
    public String getFirstName()
    {
        return firstName;
    }

    @Override
    public void setFirstName( final String firstName )
    {
        this.firstName = firstName;
    }

    @Override
    public String getLastName()
    {
        return lastName;
    }

    @Override
    public void setLastName( final String lastName )
    {
        this.lastName = lastName;
    }

    @Override
    public String getEmail()
    {
        return email;
    }

    @Override
    public void setEmail( final String email )
    {
        this.email = email;
    }

    @Override
    public void addRole( final Role role )
    {
        if ( roles == null )
        {
            roles = new HashSet<Role>();
        }

        roles.add( role );
    }

    @Override
    public Set<Role> getRoles()
    {
        return roles;
    }

    @Override
    public void setRoles( final Set<Role> roles )
    {
        this.roles = roles;
    }

    public int getId()
    {
        return id;
    }

    public void setId( final int id )
    {
        this.id = id;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals( final Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        final DBUser other = (DBUser) obj;
        if ( !username.equals( other.username ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public AuthenticationInfo getAuthenticationInfo()
    {
        return new SimpleAuthenticationInfo( username, passwordDigest, realm );
    }

    @Override
    public DBUser updateFrom( final User user )
    {
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.passwordDigest = user.getPasswordDigest();

        return this;
    }

}
