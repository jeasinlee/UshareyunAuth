package cn.ushare.account.util;

import cn.ushare.account.dto.LdapUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import static cn.ushare.account.util.StringUtil.isEnable;

/**
 * 将ldap返回的结果，转成指定对象
 */
@Slf4j
public class LdapUserAttributeMapper implements AttributesMapper {

    /**
     * 将单个Attributes转成单个对象
     *
     * @param attrs
     * @return
     * @throws NamingException
     */
    @Override
    public Object mapFromAttributes(Attributes attrs) throws NamingException {
        LdapUser user = new LdapUser();

        if (attrs.get("SamaccountName") != null) {
            user.setUid(attrs.get("SamaccountName").get().toString());
        }
        if (attrs.get("UserPassword") != null) {
            user.setUserPassword(attrs.get("UserPassword").get().toString());
        }
        if (attrs.get("displayName") != null) {
            user.setUserCn(attrs.get("displayName").get().toString());
        }
        if (attrs.get("mobile") != null) {
            user.setMobile(attrs.get("mobile").get().toString());
        }
        if (attrs.get("mail") != null) {
            user.setMail(attrs.get("mail").get().toString());
        }
        if (attrs.get("userAccountControl") != null) {
            log.error("===userACC:" + attrs.get("userAccountControl"));
            user.setEnable(isEnable(attrs.get("userAccountControl").toString()));
        }

        return user;
    }
}
