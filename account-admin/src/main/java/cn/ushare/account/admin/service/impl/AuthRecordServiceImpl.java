package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.config.GlobalCache;
import cn.ushare.account.admin.mapper.AuthRecordMapper;
import cn.ushare.account.admin.service.AcService;
import cn.ushare.account.admin.service.AuthRecordService;
import cn.ushare.account.admin.service.AuthUserService;
import cn.ushare.account.admin.service.EmployeeService;
import cn.ushare.account.entity.*;
import cn.ushare.account.util.DateTimeUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jixiang.li
 * @since 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class AuthRecordServiceImpl extends ServiceImpl<AuthRecordMapper, AuthRecord> implements AuthRecordService {

    @Autowired
    AuthRecordMapper authRecordMapper;
    @Autowired
    AcService acService;
    @Autowired
    GlobalCache globalCache;
    @Autowired
    AuthUserService authUserService;
    @Autowired
    EmployeeService employeeService;

    // 加入认证记录
    @Override
    public BaseResult add(AuthParam authParam, boolean macPrior) {
        AuthRecord authRecord = new AuthRecord();
        Integer authMethod = authParam.getAuthMethod();
        authRecord.setAuthMethod(authMethod);
        authRecord.setPhone(authParam.getPhone());
        Integer userType = null;
        if (null!=authParam.getUserName() && authParam.getUserName().equals("portalDefaultAccount")) {
            userType = 1;// 访客
        } else {
            userType = 0;// 员工
        }
        authRecord.setUserType(userType);
        // 员工授权认证，记录访客姓名、电话、授权员工ID
        if (authMethod == Constant.AuthMethod.EMPLOYEE_AUTH) {
            authRecord.setFullName(authParam.getGuestName());
            authRecord.setPhone(authParam.getGuestPhone());
            // 根据id查找授权员工ID
            Employee authEmployee = employeeService.getById(authParam.getAuthEmployeeId());
            authRecord.setAuthEmployeeId(null != authEmployee ? authEmployee.getId() : -1);
            authRecord.setAuthEmployeeName(null != authEmployee ? authEmployee.getUserName(): "");
        // 其他认证方式，记录userName
        }

        authRecord.setUserName(authParam.getUserName());

        // 用于展示的用户账号
        if (authMethod == Constant.AuthMethod.ACCOUNT_AUTH) {
            authRecord.setShowUserName(authParam.getGuestName());
        } else if (authMethod == Constant.AuthMethod.SMS_AUTH) {
            authRecord.setShowUserName(authParam.getPhone());
        } else if (authMethod == Constant.AuthMethod.WX_AUTH) {
            authRecord.setShowUserName("微信用户");
        } else if (authMethod == Constant.AuthMethod.ONEKEY_AUTH) {
            authRecord.setShowUserName("一键登录账号");
        } else if (authMethod == Constant.AuthMethod.EMPLOYEE_AUTH) {
            authRecord.setShowUserName(authParam.getGuestName());
            authRecord.setAuthEmployeeId(authParam.getAuthEmployeeId());
            authRecord.setAuthEmployeeName(authParam.getAuthEmployeeName());
        } else if (authMethod == Constant.AuthMethod.QRCODE_AUTH) {
            authRecord.setShowUserName("二维码登录账号");
        } else if(authMethod == Constant.AuthMethod.DING_TALK_AUTH){
            authRecord.setShowUserName(authParam.getGuestName());
        }

        authRecord.setIp(authParam.getUserIp());
        authRecord.setMac(authParam.getUserMac());
        authRecord.setAcIp(authParam.getAcIp());
        authRecord.setAcMac(authParam.getAcMac());
        authRecord.setSsid(authParam.getSsid());
        authRecord.setApIp(authParam.getApIp());
        authRecord.setApMac(authParam.getApMac());
//        authRecord.setOnlineState(1);
        authRecord.setIsValid(0);
        authRecord.setLastOnlineTime(new Date());
        authRecord.setTerminalType(authParam.getTerminalType());
        authRecord.setMacPrior(macPrior ? 1 : 0);
        authRecord.setPhone(authParam.getPhone());

        authRecordMapper.insert(authRecord);
        return new BaseResult();
    }

    /**
     * 根据userIp更新计费信息：流量、acctSessionId
     */
    @Override
    public BaseResult updateAccountInfo(String userIp, String userMac, Long upFlow, Long downFlow,
                                        String acctSessionId, Integer state) {
        AuthRecord authRecord = authRecordMapper.getTopOneByMac(userMac);
        if (authRecord != null) {
            authRecord.setIp(userIp);
            authRecord.setUpDataFlow(upFlow);
            authRecord.setDownDataFlow(downFlow);
            authRecord.setOnlineState(state);
            authRecord.setIsValid(1);
            authRecord.setAcctSessionId(acctSessionId);
            authRecordMapper.updateById(authRecord);
        } else {
            authRecord = authRecordMapper.getTopOne(userIp);
            if(null!=authRecord){
                authRecord.setUpDataFlow(upFlow);
                authRecord.setMac(userMac);
                authRecord.setDownDataFlow(downFlow);
                authRecord.setOnlineState(state);
                authRecord.setIsValid(1);
                authRecord.setAcctSessionId(acctSessionId);
                authRecordMapper.updateById(authRecord);
            }
        }
        return new BaseResult();
    }

    /**
     * 根据userIp更新acctSessionId，包括认证记录表和认证用户表
     */
    @Override
    public BaseResult updateAcctSessionId(String userIp, String acctSessionId) {
        // 更新认证记录
        AuthRecord authRecord = authRecordMapper.getTopOne(userIp);
        authRecord.setAcctSessionId(acctSessionId);
        authRecordMapper.updateById(authRecord);

        // 更新认证用户
        AuthUser authUser = new AuthUser();
        authUser.setAcctSessionId(acctSessionId);
        authUser.setMac(authRecord.getMac());// mac值从authRecord中查
        authUserService.updateByMac(authUser);

        return new BaseResult();
    }

    @Override
    public BaseResult statisticAuthMethod() {
        List<Map<String, Object>> list = authRecordMapper.statisticAuthMethod();
        return new BaseResult(list);
    }

    @Override
    public Page<AuthRecord> getList(Page<AuthRecord> page, QueryWrapper wrapper) {
        return page.setRecords(authRecordMapper.getList(page, wrapper));
    }

    @Override
    public Long countToday() {
        return authRecordMapper.countToday();
    }

    @Override
    public AuthRecord getTopOne(String userIp) {
        return authRecordMapper.getTopOne(userIp);
    }

    /**
     * 流量top
     * type：1：今天，2：最近7天，3：最近1月
     */
    @Override
    public List<AuthRecord> statisticFlowTop(Integer type) {
        String dayStr = null;
        if (type == 1) {
            dayStr = DateTimeUtil.date_sdf.format(new Date());
        } else if (type == 2) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -6);
            Date weekTime = calendar.getTime();
            dayStr = DateTimeUtil.date_sdf.format(weekTime);
        } else if (type == 3) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            Date monthTime = calendar.getTime();
            dayStr = DateTimeUtil.date_sdf.format(monthTime);
        }
        List<AuthRecord> list = authRecordMapper.flowTop(dayStr);

        // byte转为M
        for (int i = 0; i < list.size(); i++) {
            AuthRecord item = list.get(i);
            item.setDataFlow(item.getDataFlow() == null ? 0 : item.getDataFlow() / 1024 / 1024);
            item.setDownDataFlow(item.getDownDataFlow() == null ? 0 : item.getDownDataFlow() / 1024 / 1024);
            item.setUpDataFlow(item.getUpDataFlow() == null ? 0 : item.getUpDataFlow() / 1024 / 1024);
        }
        return list;
    }

    /**
     * 在线时长top
     * type：1：今天，2：最近7天，3：最近1月
     */
    @Override
    public List<AuthRecord> statisticPeriodTop(Integer type) {
        String dayStr = null;
        if (type == 1) {
            dayStr = DateTimeUtil.date_sdf.format(new Date());
        } else if (type == 2) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -6);
            Date weekTime = calendar.getTime();
            dayStr = DateTimeUtil.date_sdf.format(weekTime);
        } else if (type == 3) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            Date monthTime = calendar.getTime();
            dayStr = DateTimeUtil.date_sdf.format(monthTime);
        }

        // 秒转小时
        List<AuthRecord> list = authRecordMapper.periodTop(dayStr);
        for (int i = 0; i < list.size(); i++) {
            AuthRecord item = list.get(i);
            float duration = 0;
            if (item.getLastOnlineDuration() != null) {
                duration = ((float) item.getLastOnlineDuration()) / 3600;
            }
            item.setLastOnlineDurationHour(duration);
        }
        return list;
    }

    @Override
    public BaseResult statisticTerminalType() {
        List<Map<String, Object>> list = authRecordMapper.statisticTerminalType();
        return new BaseResult(list);
    }

    /**
     * 认证用户数量统计
     * type：1：昨天，2：最近7天，3：最近1个月
     */
    @Override
    public BaseResult statisticAuthUserNum(Integer type) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (type == 1) {// 昨天
            Calendar ca = Calendar.getInstance();
            ca.add(Calendar.DATE, -1);
            Date yesterday = ca.getTime();
            String yesterdayStr = (new SimpleDateFormat("yyyy-MM-dd")).format(yesterday);
            String queryTime;
            for (int i = 0; i < 24; i++) {
                String hour;
                if (i < 10) {
                    queryTime = yesterdayStr + " 0" + i;// yyyy-MM-dd 01
                    hour = "0" + i + ":00";
                } else {
                    queryTime = yesterdayStr + " " + i;// yyyy-MM-dd 10
                    hour = i + ":00";
                }
                Integer num = authRecordMapper.countByHour(queryTime);
                Map<String, Object> map = new HashMap<>();
                map.put("name", hour);
                map.put("num", num);
                list.add(map);
            }
        } else if (type == 2) {// 最近7天
            for (int i = 6; i >= 0; i--) {
                Calendar ca = Calendar.getInstance();
                ca.add(Calendar.DATE, -i);
                Date day = ca.getTime();
                String dayStr = (new SimpleDateFormat("yyyy-MM-dd")).format(day);
                Integer num = authRecordMapper.countByDay(dayStr);
                Map<String, Object> map = new HashMap<>();
                map.put("name", dayStr.substring(5));
                map.put("num", num);
                list.add(map);
            }
        } else if (type == 3) {// 最近1个月
            for (int i = 29; i >= 0; i--) {
                Calendar ca = Calendar.getInstance();
                ca.add(Calendar.DATE, -i);
                Date day = ca.getTime();
                String dayStr = (new SimpleDateFormat("yyyy-MM-dd")).format(day);
                Integer num = authRecordMapper.countByDay(dayStr);
                Map<String, Object> map = new HashMap<>();
                map.put("name", dayStr.substring(5));
                map.put("num", num);
                list.add(map);
            }
        }

        return new BaseResult(list);
    }

    /**
     * 认证用户数量统计
     * type：1：昨天，2：本周（周一到周日），3：本月（1号到31号分4周算）
     */
//    @Override
    public BaseResult statisticAuthUserNumOld(Integer type) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (type == 1) {// 昨天
            Calendar ca = Calendar.getInstance();
            ca.add(Calendar.DATE, -1);
            Date yesterday = ca.getTime();
            String yesterdayStr = (new SimpleDateFormat("yyyy-MM-dd")).format(yesterday);
            String queryTime = null;
            for (int i = 0; i < 24; i++) {
                String hour = null;
                if (i < 10) {
                    queryTime = yesterdayStr + " 0" + i;// yyyy-MM-dd 01
                    hour = "0" + i + ":00";
                } else {
                    queryTime = yesterdayStr + " " + i;// yyyy-MM-dd 10
                    hour = i + ":00";
                }
                Integer num = authRecordMapper.countByHour(queryTime);
                Map<String, Object> map = new HashMap<>();
                map.put("name", hour);
                map.put("num", num);
                list.add(map);
            }
        } else if (type == 2) {// 本周
            String[] weekDays = { "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
            Calendar ca = Calendar.getInstance();
            ca.setTime(new Date());
            int weekDay = ca.get(Calendar.DAY_OF_WEEK);// 1周日，2周一
            weekDay = weekDay - 1;
            if (weekDay == 0) {
                weekDay = 7;
            }
            // 从周一到今天
            for (int i = 1; i <= weekDay; i++) {
                Calendar dayCa = Calendar.getInstance();
                dayCa.add(Calendar.DATE, -(weekDay - i));
                Date monthDay = dayCa.getTime();
                String queryTime = (new SimpleDateFormat("yyyy-MM-dd")).format(monthDay);
                Integer num = authRecordMapper.countByDay(queryTime);
                Map<String, Object> map = new HashMap<>();
                map.put("name", weekDays[i - 1]);
                map.put("num", num);
                list.add(map);
            }
            // 从明天到周日
            for (int i = 1; i <= 7 - weekDay; i++) {
                Integer num = 0;
                Map<String, Object> map = new HashMap<>();
                map.put("name", weekDays[weekDay - 1 + i]);
                map.put("num", num);
                list.add(map);
            }
        } else if (type == 3) {// 本月
            Calendar ca = Calendar.getInstance();
            int year = ca.get(Calendar.YEAR);
            int month = ca.get(Calendar.MONTH) + 1;
            String monthStr = month < 10 ? "0" + month : "" + month;
            String startTime = null;
            String endTime = null;
            for (int i = 0; i < 4; i++) {
                // 每周开始日期
                int startDay = i * 7 + 1;
                if (startDay < 10) {
                    startTime = year + "-" + monthStr + "-" + "0" + startDay;
                } else {
                    startTime = year + "-" + monthStr + "-" + startDay;
                }
                // 每周结束日期
                int endDay = (i + 1) * 7;
                if (endDay < 10) {
                    endTime = year + "-" + monthStr + "-" + "0" + endDay;
                } else {
                    endTime = year + "-" + monthStr + "-" + endDay;
                }
                if (i == 3) {// 第4周则算到31号
                    endTime = year + "-" + monthStr + "-" + "31";
                }
                Integer num = authRecordMapper.countByDays(startTime, endTime);
                Map<String, Object> map = new HashMap<>();
                map.put("name", "第" + (i + 1) + "周");
                map.put("num", num);
                list.add(map);
            }
        }

        return new BaseResult(list);
    }

}
