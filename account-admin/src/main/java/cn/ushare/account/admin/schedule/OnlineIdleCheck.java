package cn.ushare.account.admin.schedule;

import java.util.ArrayList;
import java.util.List;

import cn.ushare.account.admin.portal.service.*;
import cn.ushare.account.admin.service.AcService;
import cn.ushare.account.admin.service.AuthUserService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.ushare.account.dto.AuthLogoutParam;
import cn.ushare.account.entity.Ac;
import cn.ushare.account.entity.AuthUser;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.Employee;
import cn.ushare.account.util.DateTimeUtil;
import cn.ushare.account.util.StringUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * 关闭：在线闲置检查，超过空闲时间自动下线
 */
//@Configuration
//@EnableScheduling
@Slf4j
public class OnlineIdleCheck {

	@Autowired
    AuthUserService authUserService;
	@Autowired
    HuaweiPortalService huaweiPortalService;
	@Autowired
    RuckusPortalService ruckusPortalService;
    @Autowired
    ArubaPortalService arubaPortalService;
    @Autowired
    CiscoPortalService ciscoPortalService;
	@Autowired
    H3cPortalService h3cPortalService;
	@Autowired
    RuijiePortalService ruijiePortalService;
	@Autowired
    TplinkPortalService tplinkPortalService;
	@Autowired
    AcService acService;
	@Value("${schedule.onlineIdleTime}")
    String onlineIdleTime;

	private static boolean inProcess = false;
	private static List<AuthUser> preOnlineList = new ArrayList<>();// 上一次查询时的在线用户列表

	@Scheduled(cron = "${schedule.onlineIdleTime}")
    public void scheduler() throws Exception {
	    log.debug("onlineIdleTime " + onlineIdleTime);
	    if (inProcess) {
            return;
        }
        inProcess = true;
        try {
            // 查询在线用户
            QueryWrapper<AuthUser> query = new QueryWrapper();
            query.eq("online_state", 1);
            query.eq("is_valid", 1);
            List<AuthUser> list = authUserService.list(query);

            if(CollectionUtils.isNotEmpty(list)){
                for (int i = 0; i < list.size(); i++) {
                    AuthUser curUser = list.get(i);

                    //某些有线认证暂不支持流量计费
                    if (1 != curUser.getIsWired()) {
                        // 和上一次记录中的流量对比
                        Boolean noFlowChange = false;
                        for (int j = 0; j < preOnlineList.size(); j++) {
                            AuthUser preUser = preOnlineList.get(j);
                            if (null != preUser && preUser.getId().equals(curUser.getId())) {// 查找上次的记录
                                if (null != preUser.getDownDataFlow() && preUser.getDownDataFlow().equals(curUser.getDownDataFlow())) {
                                    noFlowChange = true;// 流量没有变化
                                }
                                break;
                            }
                        }

                        // 无流量变化，强制下线
                        if (noFlowChange) {
                            BaseResult acResult = acService.getInfoByAcIp(curUser.getAcIp());
                            if (acResult.getReturnCode().equals("0")) {
                                continue;
                            }

                            // 下线参数
                            AuthLogoutParam logoutParam = new AuthLogoutParam();
                            logoutParam.setUserIp(curUser.getIp());
                            logoutParam.setUserMac(curUser.getMac());
                            logoutParam.setAcIp(curUser.getAcIp());
                            logoutParam.setNasIp(curUser.getNasIp());
                            log.debug("Idle超时，自动下线用户：" + curUser.getUserName() + ", MAC:" + curUser.getMac());

                            // 查询ac设备品牌，调用不同设备接口
                            Ac ac = (Ac) acResult.getData();
                            String brandCode = ac.getBrand().getCode();
                            BaseResult logoutResult = new BaseResult();
                            if (brandCode.contains("huawei")) {// 华为设备
                                logoutResult = huaweiPortalService.logout(logoutParam);// 设备下线
                                if (logoutResult.getReturnCode().equals("0")) {// portal下线失败，使用radiusCoa下线
                                    logoutResult = huaweiPortalService.logoutByRadius(logoutParam);
                                }
                                log.debug("自动下线 huawei result " + logoutResult.getReturnMsg());
                            } else if (brandCode.contains("ruijie")) {// 锐捷设备
                                logoutResult = ruijiePortalService.logout(logoutParam);// 设备下线
                                if (logoutResult.getReturnCode().equals("0")) {// portal下线失败，使用radiusCoa下线
                                    logoutResult = ruijiePortalService.logoutByRadius(logoutParam);
                                }
                                log.debug("自动下线 ruijie result " + logoutResult.getReturnMsg());
                            } else if (brandCode.contains("tplink")) {// tplink设备
                                logoutResult = tplinkPortalService.logout(logoutParam);// 设备下线
                                if (logoutResult.getReturnCode().equals("0")) {// portal下线失败，使用radiusCoa下线
                                    logoutResult = tplinkPortalService.logoutByRadius(logoutParam);
                                }
                                log.debug("自动下线 tplink result " + logoutResult.getReturnMsg());
                            }  else if (brandCode.contains("h3c")) {// 新华三设备
                                logoutResult = h3cPortalService.logout(logoutParam);// 设备下线
                                if (logoutResult.getReturnCode().equals("0")) {// portal下线失败，使用radiusCoa下线
                                    logoutResult = h3cPortalService.logoutByRadius(logoutParam);
                                }
                                log.debug("自动下线 h3c result " + logoutResult.getReturnMsg());
                            } else if (brandCode.contains("ruckus")){
                                logoutResult = ruckusPortalService.logoutByRadius(logoutParam);
                                log.debug("自动下线 ruckus result " + logoutResult.getReturnMsg());
                            } else if(brandCode.contains("aruba")) {
                                logoutResult = arubaPortalService.logoutByRadius(logoutParam);
                                log.debug("自动下线 aruba result " + logoutResult.getReturnMsg());
                            } else if(brandCode.contains("cisco")){
                                logoutResult = ciscoPortalService.logoutByRadius(logoutParam);
                                log.debug("自动下线 cisco result " + logoutResult.getReturnMsg());
                            }

                            // 更新在线记录状态
                            authUserService.updateOfflineState(curUser.getMac());
                        }
                    }
                }
            }

            // 保存流量值，用于下次比对
            preOnlineList.clear();
            for (int i = 0; i < list.size(); i++) {
                AuthUser item = list.get(i);
                AuthUser copy = new AuthUser();
                copy.setId(item.getId());
                copy.setDownDataFlow(item.getDownDataFlow());
                copy.setUpDataFlow(item.getUpDataFlow());
                preOnlineList.add(copy);
            }
    	} catch (Exception e) {
            log.error("Error Exception=", e);
            inProcess = false;
        }
    	inProcess = false;
    }

}
