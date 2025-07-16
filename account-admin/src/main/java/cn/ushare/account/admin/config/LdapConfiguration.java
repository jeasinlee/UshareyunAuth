package cn.ushare.account.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
public class LdapConfiguration {

    private LdapTemplate ldapTemplate;
    @Value("${ldap.url}")
    private String url;
    @Value("${ldap.base_dn}")
    private String baseDN;
    @Value("${ldap.admin_user}")
    private String userName;
    @Value("${ldap.admin_pwd}")
    private String pwd;

    @Bean
    @ConditionalOnMissingBean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        Map<String, Object> config = new HashMap();

        contextSource.setUrl(url);
        contextSource.setBase(baseDN);
        contextSource.setUserDn(userName);
        contextSource.setPassword(pwd);

        //  解决 乱码 的关键一句
        config.put("java.naming.ldap.attributes.binary", "objectGUID");

        contextSource.afterPropertiesSet(); // important
        contextSource.setPooled(true);
        contextSource.setBaseEnvironmentProperties(config);
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate() {
        if (null == ldapTemplate) {
            ldapTemplate = new LdapTemplate(contextSource());
        }
        return ldapTemplate;
    }

}
