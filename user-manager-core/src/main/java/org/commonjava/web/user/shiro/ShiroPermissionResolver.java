package org.commonjava.web.user.shiro;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.PermissionResolver;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.commonjava.web.user.data.UserDataManager;
import org.commonjava.web.user.model.Role;

public class ShiroPermissionResolver
    implements PermissionResolver, RolePermissionResolver
{

    @Inject
    private UserDataManager dataManager;

    @Override
    public Permission resolvePermission( final String permissionName )
    {
        return dataManager.getPermission( permissionName );
    }

    @Override
    public Collection<Permission> resolvePermissionsInRole( final String roleName )
    {
        final Set<Permission> perms = new HashSet<Permission>();

        final Role role = dataManager.getRole( roleName );
        if ( role.getPermissions() != null )
        {
            for ( final org.commonjava.web.user.model.Permission perm : role.getPermissions() )
            {
                perms.add( perm );
            }
        }

        return perms;
    }

}
