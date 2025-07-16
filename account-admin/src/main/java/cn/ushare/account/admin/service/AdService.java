package cn.ushare.account.admin.service;

import cn.ushare.account.dto.LdapUser;
import cn.ushare.account.util.LdapUserAttributeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public interface AdService {

    boolean ldapAuth(String username, String passWord);

    List<LdapUser> findUser(String mobile);

    List<LdapUser> findUserByName(String userName);

    List<LdapUser> getLdapUser(String objectClass,String DN);

    List<String> getAllUids();
}
