package org.commonjava.web.user.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class PermissionTest
{

    @Test
    public void setRemoval()
    {
        String name = "*";

        Set<Permission> perms = new HashSet<Permission>();
        Permission perm = new Permission( name );
        perm.setId( 1 );

        perms.add( perm );

        perms.remove( new Permission( name ) );

        assertThat( perms.isEmpty(), equalTo( true ) );
    }

}
