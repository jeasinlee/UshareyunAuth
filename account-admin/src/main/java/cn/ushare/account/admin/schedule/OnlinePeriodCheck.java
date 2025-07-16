package cn.ushare.account.admin.schedule;

import cn.ushare.account.admin.portal.service.*;
import cn.ushare.account.admin.service.AcService;
import cn.ushare.account.admin.service.AuthUserService;
import cn.ushare.account.admin.service.SystemConfigService;
import cn.ushare.account.admin.service.WhiteListService;
import cn.ushare.account.dto.AuthLogoutParam;
import cn.ushare.account.entity.Ac;
import cn.ushare.account.entity.AuthUser;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.WhiteList;
import cn.ushare.account.util.DateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 用户在线时长检查，超过上网时限，强制下线
 */
@Configuration
@EnableScheduling
@Slf4j
public class OnlinePeriodCheck {

	@Autowired
    AuthUserService authUserService;
	@Autowired
    SystemConfigService systemConfigService;
	@Autowired
    HuaweiPortalService huaweiPortalService;
	@Autowired
    RuijiePortalService ruijiePortalService;
	@Autowired
    RuckusPortalService ruckusPortalService;
	@Autowired
	TplinkPortalService tplinkPortalService;
	@Autowired
	CiscoPortalService ciscoPortalService;
	@Autowired
    ArubaPortalService arubaPortalService;
	@Autowired
    WiredPortalService wiredPortalService;
	@Autowired
    H3cPortalService h3cPortalService;
	@Autowired
    AcService acService;
	@Autowired
    IdentityCheckService identityCheckService;
	@Autowired
    WhiteListService whiteListService;

	private static boolean inProcess = false;

	@Scheduled(cron = "${schedule.onlinePeriodCheckTime}")
    public void scheduler() throws Exception {
		log.info("====OnlinePeriodCheck start");
	    // 查询在线用户
	    QueryWrapper<AuthUser> query = new QueryWrapper();
	    query.eq("online_state", 1);
	    query.eq("is_valid", 1);
	    List<AuthUser> list = authUserService.list(query);
	    if (inProcess) {
            return;
        }
        inProcess = true;
        try {
            for (int i = 0; i < list.size(); i++) {
    	        AuthUser authUser = list.get(i);
    	        Integer authMethod = authUser.getAuthMethod();
    	        Boolean rangeExpired = false;
    	        Boolean periodExpired = false;

/*    	        if(authMethod == 5 || authMethod == 6) {
					// 是否在上网时间段内
					BaseResult result = identityCheckService.inOnlinePeriod(authMethod);
					if (result.getReturnCode().equals("0")) {
						rangeExpired = true;
					}
				}*/

				if(authMethod==5 || authMethod==6 || authMethod == 2) {
					// 查询允许的上网时长
/*					BaseResult permitPeriodResult = identityCheckService.getPermitPeriod(authMethod);
					if (permitPeriodResult.getReturnCode().equals("1")) {
						if (permitPeriodResult.getData() != null) {
							Integer permitPeriod = (Integer) permitPeriodResult.getData();
							permitPeriod = permitPeriod * 60;// 分钟转换为秒
							Long onlineTime = DateTimeUtil.getMillis(authUser.getLastOnlineTime());
							Long onlinePeriod = (System.currentTimeMillis() - onlineTime) / 1000;
							if (onlinePeriod > permitPeriod) {
								periodExpired = true;
							}
						}
					}*/
					WhiteList whiteList = whiteListService.getOne(new QueryWrapper<WhiteList>()
							.eq("value", authUser.getMac()));
					if (null != whiteList) {
						if(whiteList.getExpireTime().before(new Date())){
							periodExpired = true;
						}
					}

				}

    	        // 超时，或者不在上网时间段内，下线
    	        if (rangeExpired || periodExpired) {
    	            BaseResult acResult = acService.getInfoByAcIp(
    	                    authUser.getAcIp());
    	            if (acResult.getReturnCode().equals("0")) {
    	                continue;
    	            }

    	            //先从白名单删除
					WhiteList whiteList = whiteListService.getOne(new QueryWrapper<WhiteList>()
							.eq("value", authUser.getMac()));
    	            whiteListService.removeById(whiteList);

    	            // 下线参数
					AuthLogoutParam logoutParam = new AuthLogoutParam();
					logoutParam.setUserIp(authUser.getIp());
					logoutParam.setUserMac(authUser.getMac());
					logoutParam.setAcIp(authUser.getAcIp());
					logoutParam.setNasIp(authUser.getNasIp());
					logoutParam.setMacPrior(authUser.getMacPrior());
                    if (rangeExpired) {
                        log.debug("不在上网时间段，自动下线用户：" +
                            authUser.getUserName() + ", MAC:" + authUser.getMac());
                    } else {
                        log.debug("上网超时，自动下线用户：" +
                                authUser.getUserName() + ", MAC:" + authUser.getMac());
                    }

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
					} else if (brandCode.contains("h3c")) {// 新华三设备
                        logoutResult = h3cPortalService.logout(logoutParam);// 设备下线
                        if (logoutResult.getReturnCode().equals("0")) {// portal下线失败，使用radiusCoa下线
                            logoutResult = h3cPortalService.logoutByRadius(logoutParam);
                        }
                        log.debug("自动下线 h3c result " + logoutResult.getReturnMsg());
                    } else if (brandCode.contains("ruckus")) {// ruckus设备，只支持coa强制下线
                        logoutResult = ruckusPortalService.logoutByRadius(logoutParam);
                        log.debug("自动下线 ruckus result " + logoutResult.getReturnMsg());
                    } else if (brandCode.contains("tplink")) {// tplink设备，只支持coa强制下线
						logoutResult = tplinkPortalService.logoutByRadius(logoutParam);
						log.debug("自动下线 tplink result " + logoutResult.getReturnMsg());
					} else if (brandCode.contains("cisco")) {// cisco设备，只支持coa强制下线
						logoutResult = ciscoPortalService.logoutByRadius(logoutParam);
						log.debug("自动下线 cisco result " + logoutResult.getReturnMsg());
					} else if (brandCode.contains("aruba")) {// aruba设备，只支持coa强制下线
						logoutResult = arubaPortalService.logoutByRadius(logoutParam);
						log.debug("自动下线 cisco result " + logoutResult.getReturnMsg());
					} else if (brandCode.contains("wired")) {// wired设备，只支持coa强制下线
						logoutResult = wiredPortalService.logoutByRadius(logoutParam);
						log.debug("自动下线 wired result " + logoutResult.getReturnMsg());
					}

                    // 更新在线记录状态
                    authUserService.updateOfflineState(authUser.getMac());
    	        }
      	    }

			//手动清除无效的mac
			List<WhiteList> datas = whiteListService.list(new QueryWrapper<WhiteList>()
					.eq("type", 2).eq("is_valid", 1));
			List<Integer> ids = datas.stream().filter(w-> w.getExpireTime().before(new Date())).map(WhiteList::getId).collect(Collectors.toList());
			if(CollectionUtils.isNotEmpty(ids)){
				whiteListService.removeByIds(ids);
			}
    	} catch (Exception e) {
            log.error("Error Exception=", e);
            inProcess = false;
        }

		log.info("====OnlinePeriodCheck end");
    	inProcess = false;
    }

}
