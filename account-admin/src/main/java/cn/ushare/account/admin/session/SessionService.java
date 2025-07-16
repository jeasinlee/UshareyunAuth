package cn.ushare.account.admin.session;

import cn.ushare.account.admin.service.AccountUserService;
import cn.ushare.account.entity.AccountUser;
import cn.ushare.account.entity.Administrator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class SessionService {

    @Value("${enviroment}")
    private String enviroment;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    AccountUserService accountUserService;
    /**
     * 读取缓存的administratorId
     */
    public Integer getAdminId() {
        if(!enviroment.equals("dev")) {
            return (Integer) request.getSession().getAttribute("adminId");
        }
        return 1;
    }

    /**
     * 读取缓存的administratorInfo
     */
    public Administrator getAdminInfo() {
        Administrator admin = null;
        if(enviroment.equals("dev")){
            admin = new Administrator();
            admin.setId(1);
            admin.setRoleId(1);
            admin.setPassword("12345");
            admin.setUserName("超级管理员");
        } else {
            if (request.getSession().getAttribute("adminInfo") != null) {
                admin = (Administrator) request.getSession().getAttribute("adminInfo");
            }
        }
        return admin;
    }

    public AccountUser getUserInfo() {
        AccountUser accountUser = null;
        if (enviroment.equals("dev")) {
            accountUser = accountUserService.getById(1);
        } else {
            if (request.getSession().getAttribute("userInfo") != null) {
                accountUser = (AccountUser) request.getSession().getAttribute("userInfo");
            }
        }

        return accountUser;
    }
}
