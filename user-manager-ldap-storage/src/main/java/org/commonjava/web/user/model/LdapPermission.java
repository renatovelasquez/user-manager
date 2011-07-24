package org.commonjava.web.user.model;

import static org.apache.commons.lang.StringUtils.join;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import org.hibernate.validator.constraints.NotBlank;

@Entity
public class LdapPermission
    implements Permission
{

    @NotBlank
    private String name;

    @ManyToMany
    private Set<Permission> impliedPermissions;

    public LdapPermission()
    {
    }

    public LdapPermission( final String... nameParts )
    {
        this.name = name( nameParts );
    }

    private LdapPermission( final String name, final Set<Permission> impliedPermissions )
    {
        this.name = name;
        this.impliedPermissions = impliedPermissions;
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
    public Set<Permission> getImpliedPermissions()
    {
        return impliedPermissions;
    }

    @Override
    public void setImpliedPermissions( final Set<Permission> impliedPermissions )
    {
        this.impliedPermissions = impliedPermissions;
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
        final LdapPermission other = (LdapPermission) obj;
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
        return String.format( "Permission [%s]", name );
    }

    public static String name( final String... parts )
    {
        return join( parts, ":" );
    }

    @Override
    public Permission updateFrom( final Permission perm )
    {
        impliedPermissions = perm.getImpliedPermissions();
        return this;
    }

    public static LdapPermission convert( final List<Permission> permissions )
    {
        if ( permissions == null )
        {
            return null;
        }

        final List<LdapPermission> perms = new ArrayList<LdapPermission>( permissions.size() );
        for ( final Permission perm : permissions )
        {
            perms.add( convert( perm ) );
        }

        return null;
    }

    public static Set<Permission> convert( final Set<? extends Permission> permissions )
    {
        if ( permissions == null )
        {
            return null;
        }

        final Set<Permission> perms = new LinkedHashSet<Permission>( permissions.size() );
        for ( final Permission perm : permissions )
        {
            perms.add( convert( perm ) );
        }

        return perms;
    }

    public static LdapPermission convert( final Permission permission )
    {
        return permission == null ? null : new LdapPermission( permission.getName(),
                                                                 convert( permission.getImpliedPermissions() ) );
    }

}
