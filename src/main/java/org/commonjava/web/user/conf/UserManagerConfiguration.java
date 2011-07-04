package org.commonjava.web.user.conf;

import org.commonjava.web.user.data.PasswordManager;
import org.commonjava.web.user.model.User;

public interface UserManagerConfiguration
{

    User getInitialAdminUser( final PasswordManager passwordManager );

}
