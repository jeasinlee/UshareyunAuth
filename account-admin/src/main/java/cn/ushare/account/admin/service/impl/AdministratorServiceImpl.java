package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.config.LicenceCache;
import cn.ushare.account.admin.mapper.AdministratorMapper;
import cn.ushare.account.admin.service.*;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.dto.*;
import cn.ushare.account.entity.Administrator;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jixiang.li
 * @since 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class AdministratorServiceImpl extends ServiceImpl<AdministratorMapper, Administrator> implements AdministratorService {

    @Autowired
    HttpServletRequest request;
    @Autowired
    AdministratorMapper adminMapper;
    @Autowired
    SessionService sessionService;
    @Autowired
    SmsSendService smsSendService;
    @Autowired
    FuncResourceService resourceService;
    @Autowired
    HostUrlService hostUrlService;
    @Autowired
    SystemConfigService systemConfigService;
    @Value("${ushareyun.config.account}")
    private String isAccount;
    @Autowired
    LicenceCache licenceCache;

    @Override
    public Page<Administrator> getList(Page<Administrator> page, QueryWrapper wrapper) {
        return page.setRecords(adminMapper.getList(page, wrapper));
    }

    @Override
    public BaseResult login(AdminLoginReq adminLogin) {
        String showCode = systemConfigService.getByCode("VALID_CODE");
        if("1".equals(showCode)) {
            // 检查验证码
            if (StringUtils.isBlank(adminLogin.getCheckCode())) {
                return new BaseResult("0", "验证码不能为空", null);
            }
            if (request.getSession().getAttribute("checkCode") == null) {
                request.getSession().removeAttribute("checkCode");
                return new BaseResult("0", "验证码失效", null);
            }
            String checkCode = (String) request.getSession().getAttribute("checkCode");
            if (!checkCode.equals(adminLogin.getCheckCode())) {
                request.getSession().removeAttribute("checkCode");
                return new BaseResult("0", "验证码错误", null);
            }
        }
        request.getSession().removeAttribute("checkCode");

        // 检查密码
        QueryWrapper<Administrator> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", adminLogin.getUserName());
        queryWrapper.eq("is_valid", 1);
        Administrator admin = adminMapper.selectOne(queryWrapper);
        if (admin == null) {
            return new BaseResult("0", "账号或密码错误", null);
        }
        if (!admin.getPassword().equals(adminLogin.getPassword())) {
            return new BaseResult("0", "账号或密码错误", null);
        }

        // 缓存id和信息
        request.getSession().setAttribute("adminId", admin.getId());
        request.getSession().setAttribute("adminInfo", admin);

        LicenceInfo licenceInfo = licenceCache.getLicenceInfo();
        if (null == licenceInfo || null == licenceInfo.getIsAccount() || 1 != licenceInfo.getIsAccount()) {
            isAccount = "0";
        }

        // 查询菜单列表
        BaseResult result = resourceService.getMenuPathList("1".equals(isAccount));
        List<Map<String, Object>> menuList = (List<Map<String, Object>>) result.getData();

        // 查询服务器IP
        String serverUrl = hostUrlService.getServerUrl(request);

        Map<String, Object> map = new HashMap<>();
        map.put("menuList", menuList);
        map.put("serverUrl", serverUrl);
        map.put("isAccount", (null == licenceInfo || null == licenceInfo.getIsAccount()) ? 0 : licenceInfo.getIsAccount());
        map.put("authAction", (null == licenceInfo || null == licenceInfo.getAuthAction()) ? 0 : licenceInfo.getAuthAction());
        return new BaseResult(map);
    }

    @Override
    public BaseResult logout() {
        request.getSession().setAttribute("adminId", null);
        request.getSession().setAttribute("adminInfo", null);
        return new BaseResult();
    }

    @Override
    public BaseResult changePassword(AdminChangePasswordReq param) {
        // 检查旧密码
        Administrator admin = sessionService.getAdminInfo();
        if (admin == null) {
            return new BaseResult("0", "用户登录超时", null);
        }
        if (!admin.getPassword().equals(param.getOldPassword())) {
            return new BaseResult("0", "旧密码错误", null);
        }

        // 设置新密码
        admin.setPassword(param.getNewPassword());
        adminMapper.updateById(admin);

        return new BaseResult();
    }

    @Override
    public BaseResult forgetPassword(AdminForgetPasswordReq param) {
        // 检查短信验证码
        if (StringUtils.isBlank(param.getSmsCode())) {
            return new BaseResult("0", "验证码不能为空", null);
        }
        String smsCode = (String) request.getSession().getAttribute("smsCode");
        if (!smsCode.equals(param.getSmsCode())) {
            request.getSession().removeAttribute("smsCode");
            return new BaseResult("0", "验证码错误", null);
        }

        // 查询userName对应的用户
        String userName = (String) request.getSession().getAttribute("userName");
        QueryWrapper<Administrator> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", userName);
        queryWrapper.eq("is_valid", 1);
        Administrator admin = adminMapper.selectOne(queryWrapper);
        if (admin == null) {
            return new BaseResult("0", "没有该账号", null);
        }

        // 设置新密码
        admin.setPassword(param.getNewPassword());
        adminMapper.updateById(admin);

        return new BaseResult();
    }

    /**
     * 发送短信验证码
     */
    @Override
    public BaseResult sendSmsCode(AdminSendSmsReq param) throws Exception {
        String showCode = systemConfigService.getByCode("VALID_CODE");
        if("1".equals(showCode)) {
            // 检查图片验证码
            if (StringUtils.isBlank(param.getCheckCode())) {
                return new BaseResult("0", "验证码不能为空", null);
            }
            if (request.getSession().getAttribute("checkCode") == null) {
                return new BaseResult("0", "验证码失效", null);
            }
            String rightCheckCode = (String) request.getSession()
                    .getAttribute("checkCode");
            if (!param.getCheckCode().equals(rightCheckCode)) {
                request.getSession().removeAttribute("checkCode");
                return new BaseResult("0", "验证码错误", null);
            }
        }
        request.getSession().removeAttribute("checkCode");

        // 查询userName对应的用户手机号
        QueryWrapper<Administrator> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", param.getUserName());
        queryWrapper.eq("is_valid", 1);
        Administrator admin = adminMapper.selectOne(queryWrapper);
        if (admin == null) {
            return new BaseResult("0", "没有该账号", null);
        }

        // 发送
        String smsCode = (int) ((Math.random()*9 + 1) * 1000) + "";
        BaseResult result = smsSendService.send(admin.getPhone(), smsCode, 2);
        if (result.getReturnCode().equals("1")) {
            // 缓存参数
            request.getSession().setAttribute("userName", param.getUserName());
            request.getSession().setAttribute("smsCode", smsCode);
        }
        return result;
    }

}
