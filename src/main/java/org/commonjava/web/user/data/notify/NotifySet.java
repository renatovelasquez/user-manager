package org.commonjava.web.user.data.notify;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NotifySet<T>
    implements Iterable<T>
{

    private final Set<T> items = new HashSet<T>();

    public boolean add( final T item )
    {
        return items.add( item );
    }

    public boolean isEmpty()
    {
        return items.isEmpty();
    }

    @Override
    public Iterator<T> iterator()
    {
        return items.iterator();
    }

}
