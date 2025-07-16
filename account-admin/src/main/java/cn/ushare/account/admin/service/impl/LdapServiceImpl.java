package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.service.AdService;
import cn.ushare.account.dto.LdapUser;
import cn.ushare.account.util.LdapUserAttributeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service("ldapService")
public class LdapServiceImpl implements AdService {

    @Autowired
    private LdapTemplate ldapTemplate;

    @Override
    public boolean ldapAuth(String username, String passWord) {
        EqualsFilter filter = new EqualsFilter("uid", username);
        return ldapTemplate.authenticate("", filter.toString(), passWord);
    }

    @Override
    public List<LdapUser> findUser(String mobile) {
        EqualsFilter filter = new EqualsFilter("mobile", mobile);
        List<LdapUser> users = ldapTemplate.search("", filter.encode(),
                new LdapUserAttributeMapper());
        return users;
    }

    @Override
    public List<LdapUser> findUserByName(String userName) {
        EqualsFilter filter = new EqualsFilter("uid", userName);
        List<LdapUser> users = ldapTemplate.search("", filter.encode(),
                new LdapUserAttributeMapper());
        return users;
    }

    @Override
    public List<LdapUser> getLdapUser(String objectClass, String DN) {
        if(null==objectClass || objectClass.length()<1) {
            objectClass = "organizationalPerson";
        }

        ldapTemplate.setIgnorePartialResultException(true);
        return ldapTemplate.search(
                query().where("objectclass").is(objectClass)
                        .and("distinguishedName").is(DN),
                new LdapUserAttributeMapper());
    }

    @Override
    public List<String> getAllUids() {
        ldapTemplate.setIgnorePartialResultException(true);
        return ldapTemplate.search(
                query().where("objectclass").is("person"), (AttributesMapper<String>) attrs -> (String) attrs.get("uid").get());
    }
}
