package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.*;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.dto.AccountLockedInfo;
import cn.ushare.account.entity.*;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.ExcelUtil;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.SecretAnnotation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jixiang.li
 * @date 2022-04-05
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "AccountInfoController", description = "AccountInfoController")
@RestController
@Slf4j
@RequestMapping("/account")
public class AccountInfoController {

    private final static String moduleName = "列表";
    @Autowired
    HttpServletRequest request;
    @Autowired
    HttpServletResponse response;
    @Autowired
    SessionService sessionService;
    @Autowired
    AccountUserService accountUserService;

    @Autowired
    AccountChargePolicyService policyService;
    @Autowired
    AccountChargeRecordService debtService;
    @Autowired
    AccountUserGroupService userGroupService;
    @Autowired
    AccountUserLockedService lockedService;
    @Autowired
    AccountOrdersService accountOrdersService;
    @Autowired
    AuthRecordService authRecordService;
    @Autowired
    AccountSalesStatisticService salesStatisticService;

    @ApiOperation(value="添加/修改套餐", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/save_policy", method={RequestMethod.POST})
    public BaseResult<AccountUser> savePolicy(@RequestBody @Valid String chargePolicyJson) throws Exception {
        AccountChargePolicy chargePolicy = JsonObjUtils.json2obj(chargePolicyJson, AccountChargePolicy.class);
        return policyService.addOrUpdate(chargePolicy);
    }

    @ApiOperation(value="获取套餐列表", notes="")
    @SystemLogTag(description="获取套餐列表", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/policy_records", method={RequestMethod.POST})
    public BaseResult<BasePageResult<AccountChargePolicy>> getChargePolicy(@RequestBody String paramJson) throws Exception {
        Map param = JsonObjUtils.json2map(paramJson);
        Page<AccountChargePolicy> page = new Page<>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("policyName") && !"".equals(queryParams.get("policyName").toString())) {
                wrapper.like("policy_name", queryParams.get("policyName"));
            }
        }
        wrapper.eq("is_valid", 1);

        page = policyService.getList(page, wrapper);

        return new BaseResult(page);
    }

    @ApiOperation(value="删除套餐", notes="")
    @SystemLogTag(description="删除套餐", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/remove_charge_policy", method={RequestMethod.POST})
    public BaseResult delChargePolicy(@RequestBody Integer id) throws Exception {
        AccountChargePolicy policy = policyService.getById(id);
        if(null == policy){
            return new BaseResult("-1", "套餐不存在" ,"");
        }
        policy.setIsValid(0);
        policyService.updateById(policy);
        return new BaseResult();
    }

    @ApiOperation(value="获取充值记录", notes="")
    @SystemLogTag(description="获取充值记录", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/orders", method={RequestMethod.POST})
    public BaseResult<BasePageResult<AccountOrders>> getOrders(@RequestBody String paramJson) throws Exception {
        Map param = JsonObjUtils.json2map(paramJson);
        Page<AccountOrders> page = new Page<>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper<AccountOrders> wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("userName") && !"".equals(queryParams.get("userName").toString())) {
                wrapper.and(q -> q.like("from_login_name", queryParams.get("userName"))
                        .or().eq("to_login_name", queryParams.get("userName")));
            }
            if (null != queryParams.get("orderNum") && !"".equals(queryParams.get("orderNum").toString())) {
                wrapper.like("order_num", queryParams.get("orderNum"));
            }
            if (null != queryParams.get("policyId") && !"".equals(queryParams.get("policyId").toString())) {
                wrapper.eq("policy_id", queryParams.get("policyId"));
            }
            if (null != queryParams.get("orderStatus") && !"".equals(queryParams.get("orderStatus").toString())) {
                wrapper.eq("order_status", queryParams.get("orderStatus"));
            }
            if (null != queryParams.get("chargeStatus") && !"".equals(queryParams.get("chargeStatus").toString())) {
                wrapper.eq("charge_status", queryParams.get("chargeStatus"));
            }
            if (null != queryParams.get("payType") && !"".equals(queryParams.get("payType").toString())) {
                wrapper.eq("pay_type", queryParams.get("payType"));
            }
        }
        AccountUser userInfo = sessionService.getUserInfo();
        if(null!=userInfo){
            wrapper.eq("from_login_name", userInfo.getLoginName());
        }

        wrapper.orderByDesc("create_time");
        page = accountOrdersService.getList(page, wrapper);

        return new BaseResult(page);
    }

    @ApiOperation(value="下载充值报表", notes="{'start':'2022-04-05','end':'2022-05-01'}")
    @SystemLogTag(description="下载充值报表", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/orders_download", method = {RequestMethod.GET})
    public void downloadOrders() throws Exception {
        SimpleDateFormat fullFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd");

        QueryWrapper<AccountOrders> wrapper = new QueryWrapper();
        // 起始日期 startDate
        String start = request.getParameter("start");
        if (StringUtils.isNotBlank(start)) {
            Date startDate = simpleFormatter.parse(start);

            wrapper.ge("update_time", startDate);
        }

        // 结束日期 endDate
        String end = request.getParameter("end");
        if (StringUtils.isNotBlank(end)) {
            Date endDate = simpleFormatter.parse(end);

            wrapper.lt("update_time", fullFormatter.format(endDate).substring(0, 11) + "23:59:59");
        }

        // 订单状态
        wrapper.eq("order_status", 1); //已付款订单
//        wrapper.eq("charge_status", 1); //已充值订单
        wrapper.orderByDesc("create_time");

        List<AccountOrders> orders = accountOrdersService.list(wrapper);
        List<List<String>> excelData = new ArrayList<>();

        List<String> head = new ArrayList<>();
        head.add("产品名称");
        head.add("平台订单号");
        head.add("微信订单号");
        head.add("充值账号");
        head.add("受让账号");
        head.add("支付金额");
        head.add("充值时间");
        head.add("支付方式");

        String sheetName = "订单报表";
        StringBuffer fileNameBuff = new StringBuffer("订单对账报表-");
        if(StringUtils.isNotBlank(start)){
            fileNameBuff.append(start);
            if(StringUtils.isNotBlank(end)){
                fileNameBuff.append("至");
                fileNameBuff.append(end);
            } else {
                fileNameBuff.append("从");
                fileNameBuff.append(start);
            }
        }else {
            if(StringUtils.isNotBlank(end)){
                fileNameBuff.append("截止");
                fileNameBuff.append(end);
            }else{
                fileNameBuff.append("截止今日");
            }
        }

        orders.stream().forEach( o-> {
            List<String> data = new ArrayList<>();
            data.add(o.getProductName());
            data.add(o.getOrderNum());
            data.add(o.getPlatformOrderNum());
            data.add(o.getFromLoginName());
            data.add(o.getToLoginName());

            data.add(new BigDecimal(o.getTotalFee()).divide(new BigDecimal(100)).setScale(2, RoundingMode.DOWN).toString());
            data.add(fullFormatter.format(o.getChargeTime()));
            data.add(o.getPayType()==0?"支付宝":"微信");

            excelData.add(data);
        });

        ExcelUtil.exportExcel(response, head, excelData, sheetName, fileNameBuff.toString(), 25);
    }

    @ApiOperation(value="销售趋势", notes="{'type':'0（日），1（月），2（年）'}")
    @SystemLogTag(description="销售趋势", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/sale_trend", method={RequestMethod.POST})
    public BaseResult saleTrend(@RequestBody String jsonStr) throws Exception {
        JSONObject jsonObject = JSONObject.fromObject(jsonStr);
        //type:0（日），1（月），2（年）
        int type = jsonObject.optInt("type", 0);
        switch (type) {
            case 0:
                //获取近30条记录
                IPage<AccountSalesStatistic> dayPage = new Page<>();
                dayPage.setCurrent(1);
                dayPage.setSize(30);
                QueryWrapper<AccountSalesStatistic> dayWrapper = new QueryWrapper<>();
                dayWrapper.orderByDesc("id");
                dayPage = salesStatisticService.page(dayPage, dayWrapper);

                List<AccountSalesStatistic> daysRecord = dayPage.getRecords();
                Collections.reverse(daysRecord);

                List<Integer> days = daysRecord.stream().map(AccountSalesStatistic::getTotalAmount).collect(Collectors.toList());
                List<Integer> resultArr = new ArrayList<>();
                if (null != days) {
                    if (days.size() < 30) {
                        for (int i = 0; i < 30 - days.size(); i++) {
                            resultArr.add(0);
                        }
                    }
                    for (int j = 0; j < days.size(); j++) {
                        resultArr.add(days.get(j));
                    }
                } else {
                    for (int j = 0; j < 30; j++) {
                        resultArr.add(days.get(j));
                    }
                }

                return new BaseResult(resultArr);
            case 1:
                //获取近12条记录
                List<Integer> months = new ArrayList<>();
                //查询本月销售记录
                QueryWrapper<AccountSalesStatistic> monthWrapper = new QueryWrapper<>();
                monthWrapper.apply("DATE_FORMAT(day_str, '%Y-%m')=DATE_FORMAT(CURDATE(), '%Y-%m')");
                List<AccountSalesStatistic> oneMonthData = salesStatisticService.getTotalSales(monthWrapper);
                Integer oneMonth = oneMonthData.stream().mapToInt(AccountSalesStatistic::getTotalAmount).sum();
                months.add(oneMonth);

                //查询上月销售记录
                monthWrapper = new QueryWrapper<>();
                monthWrapper.apply("DATE_FORMAT(day_str, '%Y-%m')=DATE_FORMAT(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m')");
                List<AccountSalesStatistic> twoMonthData = salesStatisticService.getTotalSales(monthWrapper);
                Integer twoMonth = twoMonthData.stream().mapToInt(AccountSalesStatistic::getTotalAmount).sum();
                months.add(0, twoMonth);

                monthWrapper = new QueryWrapper<>();
                monthWrapper.apply("DATE_FORMAT(day_str, '%Y-%m')=DATE_FORMAT(DATE_SUB(curdate(), INTERVAL 2 MONTH),'%Y-%m')");
                List<AccountSalesStatistic> threeMonthData = salesStatisticService.getTotalSales(monthWrapper);
                Integer threeMonth = threeMonthData.stream().mapToInt(AccountSalesStatistic::getTotalAmount).sum();
                months.add(0, threeMonth);

                monthWrapper = new QueryWrapper<>();
                monthWrapper.apply("DATE_FORMAT(day_str, '%Y-%m')=DATE_FORMAT(DATE_SUB(curdate(), INTERVAL 3 MONTH),'%Y-%m')");
                List<AccountSalesStatistic> fourMonthData = salesStatisticService.getTotalSales(monthWrapper);
                Integer fourMonth = fourMonthData.stream().mapToInt(AccountSalesStatistic::getTotalAmount).sum();
                months.add(0, fourMonth);

                monthWrapper = new QueryWrapper<>();
                monthWrapper.apply("DATE_FORMAT(day_str, '%Y-%m')=DATE_FORMAT(DATE_SUB(curdate(), INTERVAL 4 MONTH),'%Y-%m')");
                List<AccountSalesStatistic> fiveMonthData = salesStatisticService.getTotalSales(monthWrapper);
                Integer fiveMonth = fiveMonthData.stream().mapToInt(AccountSalesStatistic::getTotalAmount).sum();
                months.add(0, fiveMonth);

                monthWrapper = new QueryWrapper<>();
                monthWrapper.apply("DATE_FORMAT(day_str, '%Y-%m')=DATE_FORMAT(DATE_SUB(curdate(), INTERVAL 5 MONTH),'%Y-%m')");
                List<AccountSalesStatistic> sixMonthData = salesStatisticService.getTotalSales(monthWrapper);
                Integer sixMonth = sixMonthData.stream().mapToInt(AccountSalesStatistic::getTotalAmount).sum();
                months.add(0, sixMonth);

                monthWrapper = new QueryWrapper<>();
                monthWrapper.apply("DATE_FORMAT(day_str, '%Y-%m')=DATE_FORMAT(DATE_SUB(curdate(), INTERVAL 6 MONTH),'%Y-%m')");
                List<AccountSalesStatistic> sevenMonthData = salesStatisticService.getTotalSales(monthWrapper);
                Integer sevenMonth = sevenMonthData.stream().mapToInt(AccountSalesStatistic::getTotalAmount).sum();
                months.add(0, sevenMonth);

                monthWrapper = new QueryWrapper<>();
                monthWrapper.apply("DATE_FORMAT(day_str, '%Y-%m')=DATE_FORMAT(DATE_SUB(curdate(), INTERVAL 7 MONTH),'%Y-%m')");
                List<AccountSalesStatistic> eightMonthData = salesStatisticService.getTotalSales(monthWrapper);
                Integer eightMonth = eightMonthData.stream().mapToInt(AccountSalesStatistic::getTotalAmount).sum();
                months.add(0, eightMonth);

                monthWrapper = new QueryWrapper<>();
                monthWrapper.apply("DATE_FORMAT(day_str, '%Y-%m')=DATE_FORMAT(DATE_SUB(curdate(), INTERVAL 8 MONTH),'%Y-%m')");
                List<AccountSalesStatistic> nineMonthData = salesStatisticService.getTotalSales(monthWrapper);
                Integer nineMonth = nineMonthData.stream().mapToInt(AccountSalesStatistic::getTotalAmount).sum();
                months.add(0, nineMonth);

                monthWrapper = new QueryWrapper<>();
                monthWrapper.apply("DATE_FORMAT(day_str, '%Y-%m')=DATE_FORMAT(DATE_SUB(curdate(), INTERVAL 9 MONTH),'%Y-%m')");
                List<AccountSalesStatistic> tenMonthData = salesStatisticService.getTotalSales(monthWrapper);
                Integer tenMonth = tenMonthData.stream().mapToInt(AccountSalesStatistic::getTotalAmount).sum();
                months.add(0, tenMonth);

                monthWrapper = new QueryWrapper<>();
                monthWrapper.apply("DATE_FORMAT(day_str, '%Y-%m')=DATE_FORMAT(DATE_SUB(curdate(), INTERVAL 10 MONTH),'%Y-%m')");
                List<AccountSalesStatistic> elevenMonthData = salesStatisticService.getTotalSales(monthWrapper);
                Integer elevenMonth = elevenMonthData.stream().mapToInt(AccountSalesStatistic::getTotalAmount).sum();
                months.add(0, elevenMonth);

                monthWrapper = new QueryWrapper<>();
                monthWrapper.apply("DATE_FORMAT(day_str, '%Y-%m')=DATE_FORMAT(DATE_SUB(curdate(), INTERVAL 11 MONTH),'%Y-%m')");
                List<AccountSalesStatistic> twelveMonthData = salesStatisticService.getTotalSales(monthWrapper);
                Integer twelveMonth = twelveMonthData.stream().mapToInt(AccountSalesStatistic::getTotalAmount).sum();
                months.add(0, twelveMonth);

                return new BaseResult(months);
            case 2:
                //获取近2条记录
                List<Integer> years = new ArrayList<>();
                //查询本年度销售记录
                QueryWrapper<AccountSalesStatistic> yearWrapper = new QueryWrapper<>();
                yearWrapper.apply("YEAR(day_str)=YEAR(now())");
                List<AccountSalesStatistic> curYearData = salesStatisticService.getTotalSales(yearWrapper);
                Integer curYear = curYearData.stream().mapToInt(AccountSalesStatistic::getTotalAmount).sum();
                years.add(curYear);

                //查询上年度销售记录
                yearWrapper = new QueryWrapper<>();
                yearWrapper.apply("YEAR(day_str)=YEAR(DATE_SUB(now(),interval 1 YEAR))");
                List<AccountSalesStatistic> lastYearData = salesStatisticService.getTotalSales(yearWrapper);
                Integer lastYear = lastYearData.stream().mapToInt(AccountSalesStatistic::getTotalAmount).sum();
                years.add(0, lastYear);

                return new BaseResult(years);
        }
        return new BaseResult();
    }

    @ApiOperation(value="销售类别", notes="{'day':'1（日）/30（最近30天）/365（最近1年）'}")
    @SystemLogTag(description="销售类别", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/sale_group", method={RequestMethod.POST})
    public BaseResult saleGroup(@RequestBody String jsonStr) throws Exception {
        JSONObject jsonObject = JSONObject.fromObject(jsonStr);
        //day:1（日），30（最近30天），365（最近1年）
        int day = jsonObject.optInt("day", 1);
        QueryWrapper<AccountSalesStatistic> wrapper = new QueryWrapper<>();
        wrapper.apply("DATE_SUB(curdate(), interval {0} day) <= DATE(day_str)", day);
        List<AccountSalesStatistic> datas = salesStatisticService.list(wrapper);
        Integer aliNum = null == datas ? 0 : (datas.stream().mapToInt(AccountSalesStatistic::getTotalNumAli).sum());
        Integer weixinNum = null == datas ? 0 : (datas.stream().mapToInt(AccountSalesStatistic::getTotalNumWeixin).sum());
        Map<String, Object> resultObj = new HashMap<>();
        resultObj.put("aliNum", aliNum);
        resultObj.put("weixinNum", weixinNum);

        return new BaseResult(resultObj);
    }

    @ApiOperation(value="获取账户锁定列表", notes="")
    @SystemLogTag(description="获取账户锁定列表", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/locked_users", method={RequestMethod.POST})
    public BaseResult<BasePageResult<AccountUserLocked>> getLockedUsers(@RequestBody String paramJson) throws Exception {
        Map param = JsonObjUtils.json2map(paramJson);
        Page<AccountUser> page = new Page<>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("loginName") && !"".equals(queryParams.get("loginName").toString())) {
                wrapper.like("login_name", queryParams.get("loginName"));
            }
        }
        wrapper.eq("is_locked", 1);

        page = accountUserService.getLockedList(page, wrapper);
        return new BaseResult(page);
    }

    @ApiOperation(value = "锁定用户", notes = "")
    @SystemLogTag(description = "锁定用户", moduleName = moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value = "/lock_user", method = {RequestMethod.POST})
    public BaseResult lockUser(@RequestBody AccountLockedInfo info) throws Exception {
        AccountUser accountUser = accountUserService.getById(info.getId());
        if(null == accountUser){
            return new BaseResult("-1", "用户不存在" ,"");
        }
        accountUser.setIsLocked(1);
        accountUser.setLockedReason(StringUtils.isBlank(info.getReason()) ? "系统锁定" : info.getReason());

        accountUserService.saveOrUpdate(accountUser);
        return new BaseResult();
    }


    @ApiOperation(value = "用户解除锁定", notes = "")
    @SystemLogTag(description = "用户解除锁定", moduleName = moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value = "/unlock_user", method = {RequestMethod.POST})
    public BaseResult unlockUser(@RequestBody Integer id) throws Exception {
        AccountUser accountUser = accountUserService.getById(id);
        if(null == accountUser){
            return new BaseResult("-1", "锁定用户不存在" ,"");
        }
        accountUser.setIsLocked(0);
        accountUserService.updateById(accountUser);
        return new BaseResult();
    }

    @ApiOperation(value="获取欠费账户列表", notes="")
    @SystemLogTag(description="获取欠费账户列表", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/debt_users", method={RequestMethod.POST})
    public BaseResult<BasePageResult<AccountChargeRecord>> getDebtUsers(@RequestBody String paramJson) throws Exception {
        Map param = JsonObjUtils.json2map(paramJson);
        Page<AccountUser> page = new Page<>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("loginName") && !"".equals(queryParams.get("loginName").toString())) {
                wrapper.like("login_name", queryParams.get("loginName"));
            }
        }
        wrapper.eq("is_debt", 1);

        page = accountUserService.getDebtList(page, wrapper);
        return new BaseResult(page);
    }


    @ApiOperation(value="获取分组列表", notes="")
    @SystemLogTag(description="获取分组列表", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/account_groups", method={RequestMethod.POST})
    public BaseResult<BasePageResult<AccountUserGroup>> getAccountGroups(@RequestBody String paramJson) throws Exception {
        Map param = JsonObjUtils.json2map(paramJson);
        Page<AccountUserGroup> page = new Page<>(1, 30);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("groupName") && !"".equals(queryParams.get("groupName").toString())) {
                wrapper.like("aug.group_name", queryParams.get("groupName"));
            }
        }

        page = userGroupService.getList(page, wrapper);
        return new BaseResult(page);
    }

    @ApiOperation(value = "新增/编辑分组", notes = "")
    @SystemLogTag(description = "新增/编辑分组", moduleName = moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value = "/save_account_group", method = {RequestMethod.POST})
    public BaseResult saveAccountGroup(@RequestBody @Valid String userGroupJson) throws Exception {
        AccountUserGroup userGroup = JsonObjUtils.json2obj(userGroupJson, AccountUserGroup.class);
        userGroupService.saveOrUpdate(userGroup);
        return new BaseResult();
    }

    @ApiOperation(value="删除分组", notes="")
    @SystemLogTag(description="删除分组", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/remove_account_group", method={RequestMethod.POST})
    public BaseResult delAccountGroup(@RequestBody Integer id) throws Exception {
        QueryWrapper<AccountUser> wrapper = new QueryWrapper();
        wrapper.eq("account_group_id", id);
        List<AccountUser> users = accountUserService.list(wrapper);
        if (CollectionUtils.isNotEmpty(users)) {
            return new BaseResult("-1", "该分组存在用户，不能删除！", null);
        }

        boolean flag = userGroupService.removeById(id);
        log.debug("删除分组：" + flag);
        return new BaseResult();
    }

}
