package org.commonjava.web.user.model;

import java.util.ArrayList;
import java.util.List;

public final class GeneralizationUtils
{

    private GeneralizationUtils()
    {
    }

    public static List<User> generalizeUsers( final List<? extends User> users )
    {
        return generalize( users );
    }

    public static List<Role> generalizeRoles( final List<? extends Role> roles )
    {
        return generalize( roles );
    }

    public static List<Permission> generalizePermissions( final List<? extends Permission> permissions )
    {
        return generalize( permissions );
    }

    private static <T> List<T> generalize( final List<? extends T> elements )
    {
        if ( elements == null )
        {
            return null;
        }

        final List<T> result = new ArrayList<T>();
        for ( final T element : elements )
        {
            result.add( element );
        }

        return result;
    }

}
