package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.*;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.AuthRecord;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.util.SecretAnnotation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "StatisticController", description = "统计")
@RestController
@Slf4j
@RequestMapping("/statistic")
public class StatisticController {

    private final static String moduleName = "统计";

    @Autowired
    SessionService sessionService;
    @Autowired
    AcService acService;
    @Autowired
    OnlineUserStatisticService onlineUserStatisticService;
    @Autowired
    AuthRecordService authRecordService;
    @Autowired
    AuthUserService authUserService;
    @Autowired
    SystemCmdService systemCmdService;
    @Autowired
    AdClickService adClickService;

    /**
     * 服务器状态
     */
    @ApiOperation(value="服务器状态", notes="")
    @SecretAnnotation(encode = true)
    @RequestMapping(value="/serverStatus", method={RequestMethod.POST})
    public BaseResult serverStatus() throws Exception {
        return systemCmdService.getSystemInfo();
    }

    /**
     * 在线用户按时统计
     */
    @ApiOperation(value="在线用户按时统计", notes="")
    @SecretAnnotation(encode = true)
    @RequestMapping(value="/onlineUser", method={RequestMethod.POST})
    public BaseResult onlineUser() throws Exception {
        return onlineUserStatisticService.getStatistic();
    }

    /**
     * 认证方式占比
     */
    @ApiOperation(value="认证方式占比", notes="")
    @SecretAnnotation(encode = true)
    @RequestMapping(value="/authMethod", method={RequestMethod.POST})
    public BaseResult authMethod() throws Exception {
        return authRecordService.statisticAuthMethod();
    }

    /**
     * 终端类型
     */
    @ApiOperation(value="终端类型", notes="")
    @SecretAnnotation(encode = true)
    @RequestMapping(value="/terminalType", method={RequestMethod.POST})
    public BaseResult terminalType() throws Exception {
        return authRecordService.statisticTerminalType();
    }

    /**
     * 认证人数统计
     * @param type：1昨天 ，2本周，3本月
     */
    @ApiOperation(value="认证人数统计", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/authUserNum", method={RequestMethod.POST})
    public BaseResult authUserNum(@RequestBody String type) throws Exception {
        return authRecordService.statisticAuthUserNum(Integer.parseInt(type));
    }

    /**
     * 认证次数统计
     */
    @ApiOperation(value="认证次数统计", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/authNum", method={RequestMethod.POST})
    public BaseResult authNum() throws Exception {
        // 日
        Long todayNum = authRecordService.countToday();

        // 周
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        Date weekTime = calendar.getTime();
        QueryWrapper weekQuery = new QueryWrapper();
        weekQuery.gt("create_time", weekTime);
        weekQuery.eq("is_valid", 1);
        long weekNum = authRecordService.count(weekQuery);

        // 月
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date monthTime = calendar.getTime();
        QueryWrapper monthQuery = new QueryWrapper();
        monthQuery.gt("create_time", monthTime);
        monthQuery.eq("is_valid", 1);
        long monthNum = authRecordService.count(monthQuery);

        // 全部
        QueryWrapper totalQuery = new QueryWrapper();
        totalQuery.eq("is_valid", 1);
        long totalNum = authRecordService.count(totalQuery);

        Map<String, Long> map = new HashMap<>();
        map.put("today", todayNum);
        map.put("week", weekNum);
        map.put("month", monthNum);
        map.put("total", totalNum);

        return new BaseResult(map);
    }

    /**
     * 广告点击统计TOP
     */
    @ApiOperation(value="广告点击统计", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/adClick", method={RequestMethod.POST})
    public BaseResult adClick(@RequestBody String days) throws Exception {
        List<Map<String, Integer>> list = adClickService.statisticInDays(Integer.parseInt(days));
        return new BaseResult(list);
    }

    /**
     * 按天统计新增用户数
     */
    @ApiOperation(value="按天统计新增用户数", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/statisticNewUserDaily", method={RequestMethod.POST})
    public BaseResult statisticNewUserDaily(@RequestBody String paramJson) throws Exception {
        JSONObject param = JSONObject.fromObject(paramJson);
        Page page = new Page((Integer) param.get("current"),
                (Integer) param.get("size"));
        return new BaseResult(authUserService.statisticNewUserDaily(page));
    }

    /**
     * 流量top，
     * type：1：今天，2：最近7天，3：最近1月
     */
    @ApiOperation(value="流量top", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/flowTop", method={RequestMethod.POST})
    public BaseResult flowTop(@RequestBody String type) throws Exception {
        List<AuthRecord> list = authRecordService.statisticFlowTop(Integer.parseInt(type));
        return new BaseResult(list);
    }

    /**
     * 在线时长top，
     * type：1：今天，2：最近7天，3：最近1月
     */
    @ApiOperation(value="在线时长top", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/periodTop", method={RequestMethod.POST})
    public BaseResult periodTop(@RequestBody String type) throws Exception {
        List<AuthRecord> list = authRecordService.statisticPeriodTop(Integer.parseInt(type));
        return new BaseResult(list);
    }

    @ApiOperation(value="服务器信息接口", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/status", method={RequestMethod.POST})
    public BaseResult status(@RequestBody String paramJson) throws Exception {
        BaseResult systemStatus = systemCmdService.getSystemStatus();
        return systemStatus;
    }

}
