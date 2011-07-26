package org.commonjava.web.user.inject;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.commonjava.util.logging.Logger;
import org.commonjava.web.user.data.UserDataException;
import org.commonjava.web.user.data.UserDataInitializer;

@WebListener
@Singleton
public class AdminInjector
    implements ServletContextListener
{

    private final Logger logger = new Logger( getClass() );

    private boolean finished = false;

    @Inject
    private UserDataInitializer initializer;

    @Override
    public void contextInitialized( final ServletContextEvent sce )
    {
        if ( finished )
        {
            return;
        }

        try
        {
            initializer.initializeAdmin();
        }
        catch ( final UserDataException e )
        {
            logger.error( "Failed to initialize admin-level access: %s", e, e.getMessage() );
        }

        finished = true;
    }

    @Override
    public void contextDestroyed( final ServletContextEvent sce )
    {
    }

}
