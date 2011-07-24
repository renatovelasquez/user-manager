package org.commonjava.web.user.shiro;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.commonjava.web.user.data.UserDataManager;
import org.commonjava.web.user.model.Role;
import org.commonjava.web.user.model.User;

public class ShiroRealm
    extends AuthorizingRealm
{

    @Inject
    private UserDataManager dataManager;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo( final PrincipalCollection principals )
    {
        final Object principal = principals.getPrimaryPrincipal();
        final User user = dataManager.getUser( principal.toString() );

        final Set<String> roleNames = new HashSet<String>();
        final Set<Permission> perms = new HashSet<Permission>();
        if ( user.getRoles() != null )
        {
            for ( final Role role : user.getRoles() )
            {
                roleNames.add( role.getName() );

                for ( final Permission permission : role.getPermissions() )
                {
                    perms.add( permission );
                }
            }
        }

        return new SimpleAccount( principals, user.getPasswordDigest(), roleNames, perms );
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo( final AuthenticationToken token )
        throws AuthenticationException
    {
        if ( !( token instanceof UsernamePasswordToken ) )
        {
            throw new AuthenticationException( "Cannot use authentication token of type: " + token.getClass()
                                                                                                  .getName()
                + " with this service." );
        }

        final UsernamePasswordToken tok = (UsernamePasswordToken) token;
        final User user = dataManager.getUser( tok.getUsername() );

        return user.getAuthenticationInfo();
    }

}
