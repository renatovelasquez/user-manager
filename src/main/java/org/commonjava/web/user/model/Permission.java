package org.commonjava.web.user.model;

import static org.apache.commons.lang.StringUtils.join;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.hibernate.validator.constraints.NotBlank;

@Entity
public class Permission
    implements org.apache.shiro.authz.Permission

{

    public static final String WILDCARD = "*";

    public static final String CREATE = "create";

    public static final String READ = "read";

    public static final String UPDATE = "update";

    public static final String DELETE = "delete";

    public static final String ADMIN = "admin";

    public static final String NAMESPACE = "permissions";

    @Id
    @GeneratedValue
    private int id;

    @NotBlank
    @Column( unique = true )
    private String name;

    @ManyToMany
    private Set<Permission> impliedPermissions;

    public Permission()
    {
    }

    public Permission( final String... nameParts )
    {
        this.name = name( nameParts );
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

    public Set<Permission> getImpliedPermissions()
    {
        return impliedPermissions;
    }

    public void setImpliedPermissions( final Set<Permission> impliedPermissions )
    {
        this.impliedPermissions = impliedPermissions;
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
        final Permission other = (Permission) obj;
        if ( !name.equals( other.name ) )
        {
            return false;
        }
        return true;
    }

    @Override
    public boolean implies( final org.apache.shiro.authz.Permission p )
    {
        return impliedPermissions.contains( p );
    }

    @Override
    public String toString()
    {
        return String.format( "Permission@%d [%s]", id, name );
    }

    public static String name( final String... parts )
    {
        return join( parts, ":" );
    }

    public Permission updateFrom( final Permission perm )
    {
        impliedPermissions = perm.getImpliedPermissions();
        return this;
    }

}