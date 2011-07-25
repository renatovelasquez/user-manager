package org.commonjava.web.user.data;

//import org.commonjava.enterprise.po.MailTemplate;

public enum UserMailTemplates
{
    NEW_USER( "user.new", "new-user", "Your New Account" );

    // private MailTemplate template;

    private UserMailTemplates( final String key, final String defaultTemplate, final String defaultSubject )
    {
        // template = new MailTemplate( key, defaultTemplate, defaultSubject );
    }

    // public MailTemplate template()
    // {
    // return template;
    // }

}
