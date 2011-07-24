package org.commonjava.web.user.model;

import java.util.Set;

public interface Permission
    extends org.apache.shiro.authz.Permission
{

    public static final String WILDCARD = "*";

    public static final String CREATE = "create";

    public static final String READ = "read";

    public static final String UPDATE = "update";

    public static final String DELETE = "delete";

    public static final String ADMIN = "admin";

    public static final String NAMESPACE = "permissions";

    String getName();

    void setName( final String name );

    Set<Permission> getImpliedPermissions();

    void setImpliedPermissions( final Set<Permission> impliedPermissions );

    Permission updateFrom( final Permission perm );

}