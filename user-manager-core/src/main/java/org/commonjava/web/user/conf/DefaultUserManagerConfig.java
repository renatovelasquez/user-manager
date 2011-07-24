package org.commonjava.web.user.conf;

import javax.enterprise.inject.Alternative;
import javax.inject.Named;

import org.commonjava.web.config.annotation.ConfigName;
import org.commonjava.web.config.annotation.SectionName;
import org.commonjava.web.user.data.PasswordManager;
import org.commonjava.web.user.model.User;

@SectionName( "user-manager" )
@Named( "standalone" )
@Alternative
public class DefaultUserManagerConfig
    implements UserManagerConfiguration
{

    private String adminEmail;

    private String adminPassword;

    private String adminFirstName;

    private String adminLastName;

    @Override
    public User setupInitialAdminUser( final User user, final PasswordManager passwordManager )
    {
        user.setEmail( adminEmail );
        user.setFirstName( adminFirstName );
        user.setLastName( adminLastName );
        user.setPasswordDigest( passwordManager.digestPassword( adminPassword ) );

        return user;
    }

    public String getAdminEmail()
    {
        return adminEmail;
    }

    public String getAdminPassword()
    {
        return adminPassword;
    }

    public String getAdminFirstName()
    {
        return adminFirstName;
    }

    public String getAdminLastName()
    {
        return adminLastName;
    }

    @ConfigName( "admin.email" )
    public void setAdminEmail( final String adminEmail )
    {
        this.adminEmail = adminEmail;
    }

    @ConfigName( "admin.password" )
    public void setAdminPassword( final String adminPassword )
    {
        this.adminPassword = adminPassword;
    }

    @ConfigName( "admin.firstname" )
    public void setAdminFirstName( final String adminFirstName )
    {
        this.adminFirstName = adminFirstName;
    }

    @ConfigName( "admin.lastname" )
    public void setAdminLastName( final String adminLastName )
    {
        this.adminLastName = adminLastName;
    }
}
