package cn.ushare.account.admin.schedule;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import cn.ushare.account.admin.config.LicenceCache;
import cn.ushare.account.admin.service.AdministratorService;
import cn.ushare.account.admin.service.AlarmSettingService;
import cn.ushare.account.admin.service.AuthUserService;
import cn.ushare.account.admin.service.SmsSendService;
import cn.ushare.account.admin.service.SystemCmdService;
import cn.ushare.account.admin.service.SystemConfigService;
import cn.ushare.account.dto.LicenceInfo;
import cn.ushare.account.entity.Administrator;
import cn.ushare.account.entity.AlarmSetting;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 警报
 */
@Configuration
@EnableScheduling
@Slf4j
public class AlarmCheck {

    @Autowired
    AlarmSettingService alarmService;
    @Autowired
    SystemCmdService systemCmdService;
    @Autowired
    AuthUserService authUserService;
    @Autowired
    SmsSendService smsService;
    @Autowired
    AdministratorService adminService;
    @Autowired
    SystemConfigService systemConfigService;
    @Autowired
    LicenceCache licenceCache;

    private static boolean inProcess = false;

    @Scheduled(cron = "${schedule.alarmCheckTime}")
    public void scheduler() throws Exception {
        if (inProcess) {
            return;
        }
        inProcess = true;
        try {
            // 查询警报设置
            Page<AlarmSetting> page = alarmService.getList(
                    new Page<AlarmSetting>(1, 10), new QueryWrapper<AlarmCheck>());

            List<AlarmSetting> list = page.getRecords();
            // 返回值是按id顺序排列的，1cpu，2内存，3硬盘，4认证数
            AlarmSetting cpuConfig = list.get(0);
            AlarmSetting memConfig = list.get(1);
            AlarmSetting hwSpaceConfig = list.get(2);
            AlarmSetting userNumConfig = list.get(3);

            // 查询系统状态
            BaseResult systemResult = systemCmdService.getSystemInfo();
            Map<String, Integer> map = (Map<String, Integer>) systemResult.getData();
            int cpuPercent = map.get("cpu");
            int memPercent = map.get("mem");
            int filePercent = map.get("file");

            log.info("0====cpuConfig:" + Integer.valueOf(cpuConfig.getThreshold())
                    + ",memConfig:" + Integer.valueOf(memConfig.getThreshold())
                    + ",hwSpaceConfig:" + Integer.valueOf(hwSpaceConfig.getThreshold()));
            log.info("1====cpu:" + cpuPercent + ",mem:" + memPercent + ",file:" + filePercent);

            // 认证用户数量
            int userNum = authUserService.count(new QueryWrapper());// 已有用户数量
            int leftUserNum = 0;
            LicenceInfo licenceInfo = (LicenceInfo) licenceCache.getLicenceInfo();
            if (null != licenceInfo) {
                Integer maxUserNum = licenceInfo.getStaAmount();
                leftUserNum = maxUserNum - userNum;
            }

            // 比较
            Boolean needAlarm = false;
            StringBuilder warningMsg = new StringBuilder();
            if (cpuConfig.getStatus() == 1 &&
                    (cpuPercent >= Integer.valueOf(cpuConfig.getThreshold()))) {
                needAlarm = true;
                //warningMsg.append("CPU占用率超过" + cpuConfig.getThreshold() + "%,");
            }
            if (memConfig.getStatus() == 1 &&
                    (memPercent >= Integer.valueOf(memConfig.getThreshold()))) {
                needAlarm = true;
                //warningMsg.append("内存占用率超过" + memConfig.getThreshold() + "%,");
            }
            if (hwSpaceConfig.getStatus() == 1 &&
                    (filePercent >= Integer.valueOf(hwSpaceConfig.getThreshold()))) {
                needAlarm = true;
                //warningMsg.append("硬盘占用率超过" + hwSpaceConfig.getThreshold() + "%,");
            }
            if (userNumConfig.getStatus() == 1
                    && (leftUserNum <= Integer.valueOf(userNumConfig.getThreshold()))) {
                needAlarm = true;
                //warningMsg.append("认证用户数量超过" + userNumConfig.getThreshold() + "%,");
            }
            if (needAlarm) {
                // 12小时内是否已经发送过
                String lastSendTime = systemConfigService.getByCode("ALARM-SMS-SEND-TIME");
                Calendar rightNow = Calendar.getInstance();
                rightNow.setTime(new Date());
                rightNow.add(Calendar.HOUR, -12);
                String validTime = DateTimeUtil.datetimeFormat.format(rightNow.getTime());
                if (lastSendTime.compareTo(validTime) > 0) {
                    // 12小时内不能重复发送
                    inProcess = false;
                    return;
                }

                // 短信内容处理
                //warningMsg.deleteCharAt(warningMsg.length() - 1);// 去掉尾巴逗号
                //String msg =  warningMsg.toString();
                //log.debug("alarmMsg " + msg);

                // 查询超级管理员手机
                Administrator admin = adminService.getById(1);
                if (StringUtils.isNotBlank(admin.getPhone())) {
                    // 发送短信
                    BaseResult sendResult = smsService.sendAlarm(admin.getPhone());
                    // 更新发送时间
                    if (sendResult.getReturnCode().equals("1")) {
                        systemConfigService.updateByCode("ALARM-SMS-SEND-TIME", DateTimeUtil.datetimeFormat.format(new Date()));
                    }
                } else {
                    log.error("系统警报短信发送失败，管理员手机号码为空");
                }
            }
        } catch (Exception e) {
            log.error("Error Exception=", e);
            inProcess = false;
        }
        inProcess = false;
    }


}
