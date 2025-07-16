package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.config.GlobalCache;
import cn.ushare.account.admin.mapper.AuthRecordMapper;
import cn.ushare.account.admin.mapper.AuthUserMapper;
import cn.ushare.account.admin.mapper.EmployeeMapper;
import cn.ushare.account.admin.mapper.WhiteListMapper;
import cn.ushare.account.admin.portal.service.*;
import cn.ushare.account.admin.service.AcService;
import cn.ushare.account.admin.service.AuthUserService;
import cn.ushare.account.admin.service.SsidService;
import cn.ushare.account.dto.AuthLogoutParam;
import cn.ushare.account.entity.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证用户表，与认证记录的区别是以mac唯一区分用户，没有重复记录，
 * 在线用户也是查这张表
 */
//不要加@Transactional，因为radiusService认证成功后，会用多线程更新相同的表，造成死锁
@Service
@Slf4j
public class AuthUserServiceImpl extends ServiceImpl<AuthUserMapper, AuthUser> implements AuthUserService {

    @Autowired
    AuthUserMapper authUserMapper;
    @Autowired
    GlobalCache globalCache;
    @Autowired
    AcService acService;
    @Autowired
    HuaweiPortalService huaweiPortalService;
    @Autowired
    H3cPortalService h3cPortalService;
    @Autowired
    RuckusPortalService ruckusPortalService;
    @Autowired
    ArubaPortalService arubaPortalService;
    @Autowired
    CiscoPortalService ciscoPortalService;
    @Autowired
    RuijiePortalService ruijiePortalService;
    @Autowired
    TplinkPortalService tplinkPortalService;
    @Autowired
    WiredPortalService wiredPortalService;
    @Autowired
    AuthRecordMapper authRecordMapper;
    @Autowired
    WhiteListMapper whiteListMapper;
    @Autowired
    EmployeeMapper employeeMapper;
    @Autowired
    SsidService ssidService;

    @Override
    public BaseResult saveOrUpdateByMac(AuthUser authUser) {
        QueryWrapper<AuthUser> queryWrapper = new QueryWrapper();
        queryWrapper.eq("is_valid", 1);
        queryWrapper.eq("mac", authUser.getMac());

        if(StringUtils.isBlank(authUser.getPhone())){
            Ssid ssidModel = ssidService.getOne(new QueryWrapper<Ssid>().eq("name", authUser.getSsid()));
            if(null!=ssidModel){
                if(1==ssidModel.getIsEmployee()){
                    Employee employee = employeeMapper.selectOne(new QueryWrapper<Employee>()
                            .like("bind_macs", authUser.getMac()));
                    if(null!=employee){
                        authUser.setPhone(employee.getPhone());
                    }
                } else {
                    List<WhiteList> whiteLists = whiteListMapper.selectList(new QueryWrapper<WhiteList>()
                            .eq("value", authUser.getMac()).eq("type", 2));
                    if(CollectionUtils.isNotEmpty(whiteLists)){
                        authUser.setPhone(whiteLists.get(0).getUserName());
                    }
                }
            }
        }

        AuthUser result = authUserMapper.selectOne(queryWrapper);
        if (result == null) {
            authUserMapper.insert(authUser);
        } else {
            authUser.setId(result.getId());
            authUserMapper.updateById(authUser);
        }
        return new BaseResult();
    }

    @Override
    public BaseResult updateByMac(AuthUser authUser) {
        QueryWrapper<AuthUser> queryWrapper = new QueryWrapper();
        queryWrapper.eq("is_valid", 1);
        queryWrapper.eq("mac", authUser.getMac());
        AuthUser result = authUserMapper.selectOne(queryWrapper);
        if (result == null) {
            return new BaseResult("0", "没有该MAC对应的用户记录", null);
        } else {
            authUser.setId(result.getId());
            authUserMapper.updateById(authUser);
        }
        return new BaseResult();
    }

    @Override
    public BaseResult offline(Long id) {
        // 下线参数
        AuthUser authUser = authUserMapper.selectById(id);

        //从白名单删除
        QueryWrapper<WhiteList> whiteListQueryWrapper = new QueryWrapper<>();
        whiteListQueryWrapper.eq("value", authUser.getMac());
        whiteListQueryWrapper.eq("type", 2);
        int whiteLists = whiteListMapper.delete(whiteListQueryWrapper);
        log.warn("delete whiteList：%{s}", whiteLists);

        AuthLogoutParam logoutParam = new AuthLogoutParam();
        logoutParam.setUserIp(authUser.getIp());
        logoutParam.setUserMac(authUser.getMac());
        logoutParam.setAcIp(authUser.getAcIp());
        logoutParam.setNasIp(authUser.getNasIp());
        logoutParam.setMacPrior(authUser.getMacPrior());

        // 设备参数
        BaseResult acResult = acService.getInfoByAcIp(
                authUser.getAcIp());
        if (acResult.getReturnCode().equals("0")) {
            return acResult;
        }
        Ac ac = (Ac) acResult.getData();
        String brandCode = ac.getBrand().getCode();

        // 下线
        BaseResult logoutResult = new BaseResult();
        if (brandCode.contains("huawei")) {// 华为设备
            logoutResult = huaweiPortalService.logout(logoutParam);
            if (logoutResult.getReturnCode().equals("0")) {// portal下线失败，使用radiusCoa下线
                logoutResult = huaweiPortalService.logoutByRadius(logoutParam);
            }
            log.debug("huawei logout result " + logoutResult.toString());
        } else if (brandCode.contains("ruijie")) {// 锐捷设备
            logoutResult = ruijiePortalService.logout(logoutParam);
            if (logoutResult.getReturnCode().equals("0")) {// portal下线失败，使用radiusCoa下线
                logoutResult = ruijiePortalService.logoutByRadius(logoutParam);
            }
            log.debug("ruijie logout result " + logoutResult.toString());
        } else if (brandCode.contains("tplink")) {// 普联设备
            logoutResult = tplinkPortalService.logout(logoutParam);
            if (logoutResult.getReturnCode().equals("0")) {// portal下线失败，使用radiusCoa下线
                logoutResult = tplinkPortalService.logoutByRadius(logoutParam);
            }
            log.debug("tplink logout result " + logoutResult.toString());
        } else if (brandCode.contains("h3c")) {// 新华三设备
            logoutResult = h3cPortalService.logout(logoutParam);
            if (logoutResult.getReturnCode().equals("0")) {// portal下线失败，使用radiusCoa下线
                logoutResult = huaweiPortalService.logoutByRadius(logoutParam);
            }
            log.debug("h3c logout success");
        } else if (brandCode.contains("ruckus")) {// ruckus设备，仅支持radiusCoa命令下线
            logoutResult = ruckusPortalService.logoutByRadius(logoutParam);
        } else if (brandCode.contains("aruba")) {// aruba设备，仅支持radiusCoa命令下线
            logoutResult = arubaPortalService.logoutByRadius(logoutParam);
        } else if (brandCode.contains("cisco")) {// cisco设备，仅支持radiusCoa命令下线
            logoutParam.setAcIp(ac.getNasIp());
            ciscoPortalService.logoutByRadius(logoutParam);
            logoutResult = new BaseResult();
        } else if (brandCode.contains("wired")) {// 有线设备，仅支持radiusCoa命令下线
            logoutResult = wiredPortalService.logoutByRadius(logoutParam);
        }

        // 下线成功更新记录到离线状态
        if (logoutResult.getReturnCode().equals("1")) {
            updateOfflineState(authUser.getMac());
        }

        // 下线失败，记录失败次数
        if (logoutResult.getReturnCode().equals("0")) {
            if (authUser.getLogoutFailNum() < 1) {// 失败次数+1
                authUser.setLogoutFailNum(authUser.getLogoutFailNum() + 1);
                authUserMapper.updateById(authUser);
            } else {// 失败2次，则更新为下线状态，返回成功
                updateOfflineState(authUser.getMac());
                return new BaseResult();
            }
        }

        return logoutResult;
    }

    /**
     * 更新离线状态
     */
    @Override
    public BaseResult updateOfflineState(String userMac) {
        // 删除临时登录记录
        globalCache.removeTempLogin(userMac);

        // 更新认证用户表的在线状态
        QueryWrapper<AuthUser> queryWrapper = new QueryWrapper();
        queryWrapper.eq("mac", userMac);
        queryWrapper.eq("is_valid", 1);
        AuthUser authUser = authUserMapper.selectOne(queryWrapper);
        if (authUser != null) {
            // 离线
            authUser.setOnlineState(0);
            authUser.setLogoutFailNum(0);
            // 在线时长
            Integer duration = (int) ((System.currentTimeMillis()
                    - authUser.getLastOnlineTime().getTime()) / 1000);
            authUser.setLastOnlineDuration(duration);
            authUserMapper.updateById(authUser);
        }
        if (null != authUser) {
            // 更新认证记录表的在线状态
            String userIp = authUser.getIp();
            AuthRecord authRecord = authRecordMapper.getTopOne(userIp);
            if (authRecord != null) {
                // 离线
                authRecord.setOnlineState(0);
                // 在线时长
                Integer duration = (int) ((System.currentTimeMillis()
                        - authRecord.getLastOnlineTime().getTime()) / 1000);
                authRecord.setLastOnlineDuration(duration);
                authRecordMapper.updateById(authRecord);
            }
        }

        return new BaseResult();
    }

    @Override
    public Page<AuthUser> getList(Page<AuthUser> page, QueryWrapper wrapper) {
//        Object paramOnlineState = map.get("onlineState");
//        if (paramOnlineState != null && paramOnlineState.getClass() == String.class) {
//            map.put("onlineState", paramOnlineState);
//        }
//        Object paramUserType = map.get("userType");
//        // 选择了类型，传值为int，不选任何类型，传值为“”，要改为null，sql中才能查询
//        if (paramUserType != null && paramUserType.getClass() == String.class) {
//            map.put("userType", paramUserType);
//        }
//
//        // 选择了类型，传值为int，不选任何类型，传值为“”，要改为null，sql中才能查询
//        Object paramAuthMethod = map.get("authMethod");
//        if (paramAuthMethod != null && paramAuthMethod.getClass() == String.class) {
//            map.put("authMethod", paramAuthMethod);
//        }
//        String userName = map.getOrDefault("userName", "").toString();
//        if (StringUtils.isNotBlank(userName)) {
//            Employee employee = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("user_name", userName));
//            if (null != employee) {
//                map.put("macs", Arrays.asList(employee.getBindMacs().split(",")));
//            } else {
//                map.put("macs", null);
//            }
//        }
//
//        String phone = map.getOrDefault("phone", "").toString();
//        if(StringUtils.isNotBlank(phone)){
//            List<String> macs = new ArrayList<>();
//            //先查询员工
//            Employee employee = employeeMapper.selectOne(new QueryWrapper<Employee>().eq("phone", phone));
//            if(null!=employee && StringUtils.isNotBlank(employee.getBindMacs())){
//                macs.addAll(Arrays.asList(employee.getBindMacs().split(",")));
//            }
//
//            List<WhiteList> whiteLists = whiteListMapper.selectList(new QueryWrapper<WhiteList>()
//                    .eq("user_name", phone).eq("type", 2));
//            if(CollectionUtils.isNotEmpty(whiteLists)){
//                macs.add(whiteLists.get(0).getValue());
//            }
//
//            map.put("macs", macs);
//        }

        return page.setRecords(authUserMapper.getList(page, wrapper));
    }

    /**
     * 流量清零
     */
    @Override
    public BaseResult resetFlow(String userIp, String userMac) {
        // 根据userIp查询认证记录
        AuthRecord authRecord = authRecordMapper.getTopOne(userIp);

        // 根据mac清空该用户流量
        if (authRecord != null) {// 初次认证时该值为空，因为该线程可能比认证成功后新增authRecord的线程早
            authUserMapper.resetFlowByMac(authRecord.getMac());
        } else {
            authRecord = authRecordMapper.getTopOneByMac(userMac);
            if(null!=authRecord){
                authUserMapper.resetFlowByMac(authRecord.getMac());
            }
        }

        return new BaseResult();
    }

    @Override
    public Page statisticNewUserDaily(Page page) {
        return page.setRecords(authUserMapper.statisticNewUserDaily(page, null));
    }

}
