package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.service.AdService;
import cn.ushare.account.dto.LdapUser;
import cn.ushare.account.util.LdapUserAttributeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Service("adService")
public class AdServiceImpl implements AdService {

    @Autowired
    private LdapTemplate ldapTemplate;

    @Override
    public boolean ldapAuth(String username, String passWord) {
        EqualsFilter filter = new EqualsFilter("SamaccountName", username);
        return ldapTemplate.authenticate("", filter.toString(), passWord);
    }

    @Override
    public List<LdapUser> findUser(String mobile) {
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("mobile", mobile));
//        filter.and(new EqualsFilter("userAccountControl", "66048"));
        List<LdapUser> users = ldapTemplate.search("", filter.encode(),
                new LdapUserAttributeMapper());
        return users.stream().filter(LdapUser::getEnable).collect(Collectors.toList());
    }

    @Override
    public List<LdapUser> findUserByName(String userName) {
        AndFilter filter = new AndFilter();
        filter.and(new EqualsFilter("SamaccountName", userName));
//        filter.and(new EqualsFilter("userAccountControl", "66048"));
        List<LdapUser> users = ldapTemplate.search("", filter.encode(),
                new LdapUserAttributeMapper());
        return users.stream().filter(LdapUser::getEnable).collect(Collectors.toList());
    }

    @Override
    public List<LdapUser> getLdapUser(String objectClass, String DN) {
        if(null==objectClass || objectClass.length()<1) {
            objectClass = "organizationalPerson";
        }

        ldapTemplate.setIgnorePartialResultException(true);
        List<LdapUser> users = ldapTemplate.search(
                query().where("objectclass").is(objectClass)
                        .and("objectCategory").is(DN),
                new LdapUserAttributeMapper());
        return users.stream().filter(LdapUser::getEnable).collect(Collectors.toList());
    }

    @Override
    public List<String> getAllUids() {
        ldapTemplate.setIgnorePartialResultException(true);
        return ldapTemplate.search(
                query().where("userAccountControl").is("66048"), (AttributesMapper<String>) attrs -> (String) attrs.get("SamaccountName").get());
    }
}
