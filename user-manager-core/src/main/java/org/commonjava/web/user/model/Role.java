package org.commonjava.web.user.model;

import java.util.Set;

public interface Role
{

    public static final String ADMIN = "admin";

    public static final String NAMESPACE = "roles";

    String getName();

    void setName( final String name );

    void addPermission( final Permission permission );

    void removePermission( final Permission permission );

    Set<Permission> getPermissions();

    void setPermissions( final Set<Permission> permissions );

    Role updateFrom( final Role role );

}