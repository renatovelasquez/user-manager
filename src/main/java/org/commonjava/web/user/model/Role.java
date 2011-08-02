package org.commonjava.web.user.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table( name = "roles" )
public class Role
{

    public static final String ADMIN = "admin";

    public static final String NAMESPACE = "roles";

    @Id
    @GeneratedValue
    private int id;

    @NotBlank
    @Column( unique = true )
    private String name;

    @ManyToMany
    private Set<Permission> permissions;

    public Role()
    {}

    public Role( final String name, final Permission... perms )
    {
        this.name = name;
        this.permissions = new HashSet<Permission>( Arrays.asList( perms ) );
    }

    public Role( final String name, final Collection<Permission> perms )
    {
        this.name = name;
        this.permissions = new HashSet<Permission>( perms );
    }

    public int getId()
    {
        return id;
    }

    public void setId( final int id )
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName( final String name )
    {
        this.name = name;
    }

    public synchronized boolean addPermission( final Permission permission )
    {
        if ( permissions == null )
        {
            permissions = new HashSet<Permission>();
        }

        return permissions.add( permission );
    }

    public boolean removePermission( final Permission permission )
    {
        if ( permissions != null )
        {
            return permissions.remove( permission );
        }

        return false;
    }

    public Set<Permission> getPermissions()
    {
        return permissions;
    }

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
        final Role other = (Role) obj;
        if ( !name.equals( other.name ) )
        {
            return false;
        }
        return true;
    }

    public boolean containsPermission( final Permission perm )
    {
        return permissions != null && permissions.contains( perm );
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "Role [id=" ).append( id ).append( ", name=" ).append( name ).append( "\n\tpermissions=" ).append( permissions ).append( "]" );
        return builder.toString();
    }

}
