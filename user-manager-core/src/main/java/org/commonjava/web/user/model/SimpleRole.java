package org.commonjava.web.user.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import org.hibernate.validator.constraints.NotBlank;

@Entity
public class SimpleRole
    implements Role
{

    @NotBlank
    @Column( unique = true )
    private String name;

    @ManyToMany
    private Set<Permission> permissions;

    public SimpleRole()
    {
    }

    public SimpleRole( final String name, final Permission... perms )
    {
        this.name = name;
        this.permissions = new HashSet<Permission>( Arrays.asList( perms ) );
    }

    public SimpleRole( final String name, final Collection<Permission> perms )
    {
        this.name = name;
        this.permissions = new HashSet<Permission>( perms );
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName( final String name )
    {
        this.name = name;
    }

    @Override
    public synchronized void addPermission( final Permission permission )
    {
        if ( permissions == null )
        {
            permissions = new HashSet<Permission>();
        }
        permissions.add( permission );
    }

    @Override
    public void removePermission( final Permission permission )
    {
        if ( permissions != null )
        {
            permissions.remove( permission );
        }
    }

    @Override
    public Set<Permission> getPermissions()
    {
        return permissions;
    }

    @Override
    public void setPermissions( final Set<Permission> permissions )
    {
        this.permissions = permissions;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
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
        final SimpleRole other = (SimpleRole) obj;
        if ( !name.equals( other.name ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public SimpleRole updateFrom( final Role role )
    {
        this.permissions = role.getPermissions();
        return this;
    }

    public static List<SimpleRole> convert( final List<Role> roles )
    {
        if ( roles == null )
        {
            return null;
        }

        final List<SimpleRole> result = new ArrayList<SimpleRole>( roles.size() );
        for ( final Role role : roles )
        {
            result.add( convert( role ) );
        }

        return result;
    }

    public static SimpleRole convert( final Role role )
    {
        return role == null ? null : new SimpleRole( role.getName(), SimplePermission.convert( role.getPermissions() ) );
    }

}
