package org.commonjava.web.user.model;

import java.util.Set;

import org.apache.shiro.authc.AuthenticationInfo;

public interface User
{

    public static final String ADMIN = "admin";

    public static final String NAMESPACE = "users";

    public static final String NOT_SPECIFIED = "";

    String getUsername();

    void setUsername( final String username );

    String getPasswordDigest();

    void setPasswordDigest( final String passwordDigest );

    String getFirstName();

    void setFirstName( final String firstName );

    String getLastName();

    void setLastName( final String lastName );

    String getEmail();

    void setEmail( final String email );

    void addRole( final Role role );

    Set<Role> getRoles();

    void setRoles( final Set<Role> roles );

    AuthenticationInfo getAuthenticationInfo();

    User updateFrom( final User user );

}