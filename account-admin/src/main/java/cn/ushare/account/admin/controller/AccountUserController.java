package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.*;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.dto.AccountUserForgetReq;
import cn.ushare.account.dto.AccountUserModifyReq;
import cn.ushare.account.entity.*;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.EncryptUtils;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.SecretAnnotation;
import cn.ushare.account.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "AccountUserController", description = "AccountUserController")
@RestController
@Slf4j
@RequestMapping("/account_user")
public class AccountUserController {

    private final static String moduleName = "列表";

    @Autowired
    SessionService sessionService;
    @Autowired
    AccountUserService accountUserService;
    @Autowired
    AccountChargePolicyService policyService;

    @Autowired
    AccountUserMacService macService;
    @Autowired
    AccountOrdersService accountOrdersService;
    @Autowired
    WhiteListService whiteListService;

    @ApiOperation(value="注册", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/reg", method={RequestMethod.POST})
    public BaseResult<AccountUser> reg(@RequestBody String accountUserJson) throws Exception {
        AccountUser user = JsonObjUtils.json2obj(accountUserJson, AccountUser.class);
        if (null == user.getId()) {
            AccountUser detail = accountUserService.getDetail(user.getLoginName(), 1);
            if (null != detail && 1 == user.getIsReg()) {
                return new BaseResult<>("-1", "账号已存在", null);
            }
        }
        return accountUserService.addOrUpdate(user);
    }

    @ApiOperation(value="登录", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/login", method={RequestMethod.POST})
    public BaseResult login(@RequestBody String accountUserJson) throws Exception {
        return accountUserService.login(accountUserJson);
    }

    @ApiOperation(value="修改密码", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/modify", method={RequestMethod.POST})
    public BaseResult<AccountUser> modify(@RequestBody AccountUserModifyReq modifyReq) throws Exception {
        AccountUser accountUser = accountUserService.getDetail(modifyReq.getUserName(), 1);
        if(null == accountUser){
            return new BaseResult<>("-1" , "账号不存在", null);
        }
        if (!accountUser.getPwd().equals(EncryptUtils.encodeBase64String(modifyReq.getOldPwd()))){
            return new BaseResult<>("-1" , "原密码错误", null);
        }
        accountUser.setPwd(EncryptUtils.encodeBase64String(modifyReq.getReplacePwd()));
        accountUserService.saveOrUpdate(accountUser);

        return new BaseResult<>();
    }

    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询详情", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    public BaseResult<BasePageResult<AccountUser>> getList(@RequestBody String paramJson) throws Exception {
        Map param = JsonObjUtils.json2map(paramJson);
        Page<AccountUser> page = new Page<>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper<AccountUser> wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("loginName") && !"".equals(queryParams.get("loginName").toString())) {
                wrapper.and( q -> q.like("au.login_name", queryParams.get("loginName")).or().like("au.nick_name", queryParams.get("loginName")));
            }
            if (null != queryParams.get("groupId") && !"".equals(queryParams.get("groupId").toString())) {
                wrapper.eq("au.account_group_id", queryParams.get("groupId"));
            }
            if (null != queryParams.get("policyId") && !"".equals(queryParams.get("policyId").toString())) {
                wrapper.eq("au.charge_policy_id", queryParams.get("policyId"));
            }
            if (null != queryParams.get("mobile") && !"".equals(queryParams.get("mobile").toString())) {
                wrapper.eq("au.mobile", queryParams.get("mobile"));
            }
            if (null != queryParams.get("isValid") && !"".equals(queryParams.get("isValid").toString())) {
                wrapper.eq("au.is_valid", queryParams.get("isValid"));
            }
        }
        page = accountUserService.getList(page, wrapper);

        return new BaseResult(page);
    }

    @ApiOperation(value="查询用户最新信息", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/detail")
    public BaseResult detail() throws Exception {
        AccountUser userInfo = sessionService.getUserInfo();
        log.debug("===userInfo:" + userInfo);
        if (null != userInfo) {
            AccountUser detail = accountUserService.getDetail(userInfo.getLoginName(), 1);
            return new BaseResult(detail);
        }

        return new BaseResult();
    }

    @ApiOperation(value="模板导入", notes="")
    @SystemLogTag(description="模板导入", moduleName=moduleName)
    @RequestMapping(value="/excelImport", method={RequestMethod.POST})
    public BaseResult excelImport(MultipartFile file) throws Exception {
        return accountUserService.excelImport(file);
    }

    @ApiOperation(value="模板导出", notes="")
    @SystemLogTag(description="模板导出", moduleName=moduleName)
    @RequestMapping(value="/excelExport", method={RequestMethod.GET})
    public void excelExport(String ids) throws Exception {
        accountUserService.excelExport(ids);
    }

    @ApiOperation(value="删除账户", notes="")
    @SystemLogTag(description="删除账户", moduleName=moduleName)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        AccountUser accountUser = accountUserService.getById(id);
        if(null!=accountUser && 1 == accountUser.getIsValid()) {
            //删除名下mac
            List<AccountUserMac> macs = macService.list(new QueryWrapper<AccountUserMac>()
                    .eq("user_id", accountUser.getId()));
            if(CollectionUtils.isNotEmpty(macs)){
                List<Integer> ids = macs.stream().map(m -> m.getId()).collect(Collectors.toList());
                log.info("删除名下macs：" + ids.toString());
                macService.removeByIds(ids);
            }

            accountUser.setIsValid(0);
            accountUserService.updateById(accountUser);
            return new BaseResult();
        } else {
            return new BaseResult("0", "账户不存在", null);
        }
    }

    @ApiOperation(value="批量删除", notes="")
    @SystemLogTag(description="批量删除", moduleName=moduleName)
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        accountUserService.updateByIds(Arrays.asList(ids));
        return new BaseResult();
    }

    @ApiOperation(value="修改账户信息", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/modify_info", method={RequestMethod.POST})
    public BaseResult modifyInfo(@RequestBody String accountUserJson) throws Exception {
        AccountUser user = JsonObjUtils.json2obj(accountUserJson, AccountUser.class);
        AccountUser dbUser = accountUserService.getById(user.getId());
        if (null == dbUser) {
            return new BaseResult("0", "账户不存在", null);
        }

        if (StringUtils.isEmpty(user.getPwd())) {
            user.setPwd(EncryptUtils.encodeBase64String(dbUser.getPwd()));
        } else {
            if (!StringUtil.validPwdForXiaoxiang(user.getPwd())) {
                return new BaseResult("0", "密码必须是8到16位数字或字母", null);
            }
            user.setPwd(EncryptUtils.encodeBase64String(user.getPwd()));
        }
        AccountChargePolicy chargePolicy = policyService.getById(user.getChargePolicyId());
        if(null == user.getBindMacNum()){
            user.setBindMacNum(chargePolicy.getBindMacNum());
        }
        if(null == user.getExpireTime()){
            Date expireT;
            switch (chargePolicy.getUnit()){
                case 0:
                    //天
                    expireT = new DateTime().plusDays(chargePolicy.getTotalNum()).toDate();
                    break;
                case 1:
                    //月
                    expireT = new DateTime().plusMonths(chargePolicy.getTotalNum()).toDate();
                    break;
                case 2:
                    //年
                    expireT = new DateTime().plusYears(chargePolicy.getTotalNum()).toDate();
                    break;
                case 3:
                    //小时
                    expireT = new DateTime().plusHours(chargePolicy.getTotalNum()).toDate();
                    break;
                default:
                    //月
                    expireT = new DateTime().plusDays(chargePolicy.getTotalNum()).toDate();
            }
            user.setExpireTime(expireT);
        }

        accountUserService.updateById(user);
        return new BaseResult();
    }

    @ApiOperation(value="忘记密码", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/forgot", method={RequestMethod.POST})
    public BaseResult forgotPwd(@RequestBody AccountUserForgetReq forgetReq) throws Exception {
        AccountUser accountUser = accountUserService.getOne(new QueryWrapper<AccountUser>()
                .eq("login_name", forgetReq.getUserName()));
        if(null == accountUser){
            return new BaseResult<>("-1" , "账号不存在", null);
        }

        return accountUserService.forgotPwd(forgetReq, accountUser);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询MAC列表", moduleName=moduleName)
    @RequestMapping(value="/getMacList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BasePageResult<AccountUserMac>> getMacList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<AccountUserMac> page = new Page<>(1, 10);
        Page<WhiteList> whiteListPage = new Page<>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper<AccountUserMac> wrapper = new QueryWrapper();
        QueryWrapper<WhiteList> whiteListQuery = new QueryWrapper<>();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("loginName") && !"".equals(queryParams.get("loginName").toString())) {
                wrapper.like("login_name", queryParams.get("loginName"));
                whiteListQuery.like("user_name",queryParams.get("loginName"));
            }
            if (null != queryParams.get("nickName") && !"".equals(queryParams.get("nickName").toString())) {
                wrapper.like("nick_name", queryParams.get("nickName"));

            }
            if (null != queryParams.get("mac") && !"".equals(queryParams.get("mac").toString())) {
                wrapper.like("mac", queryParams.get("mac"));
                whiteListQuery.like("value",queryParams.get("mac"));
            }
        }
        page = macService.getList(page, wrapper);
        Page<WhiteList> list = whiteListService.getList(whiteListPage, whiteListQuery);
        /**
         * 类型转换
         */
        List<WhiteList> whiteLists = list.getRecords();
        List<AccountUserMac> accountUserMacs = new ArrayList<>();
        for (WhiteList whiteList : whiteLists) {
            AccountUserMac accountUserMac = new AccountUserMac();
            accountUserMac.setMac(whiteList.getValue());
            accountUserMac.setLoginName(whiteList.getUserName());
            accountUserMac.setNickName(whiteList.getValue());

            accountUserMacs.add(accountUserMac);
        }
        List<AccountUserMac> records = page.getRecords();
        List<AccountUserMac> realList = new ArrayList<>();
        realList.addAll(records);
        realList.addAll(accountUserMacs);

        page.setRecords(realList);
        return new BaseResult(page);
    }

    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增mac", moduleName=moduleName)
    @RequestMapping(value="/addMac", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<AccountUserMac> addMac(@RequestBody String userMacJson) throws Exception {
        AccountUserMac userMac = JsonObjUtils.json2obj(userMacJson, AccountUserMac.class);
        return macService.add(userMac);
    }

    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改mac", moduleName=moduleName)
    @RequestMapping(value="/updateMac", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<AccountUserMac> update(@RequestBody String userMacJson) throws Exception {
        AccountUserMac userMac = JsonObjUtils.json2obj(userMacJson, AccountUserMac.class);
        return macService.update(userMac);
    }

    @ApiOperation(value="批量删除", notes="")
    @SystemLogTag(description="批量删除mac", moduleName=moduleName)
    @RequestMapping(value="/batchDeleteMac", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult batchDeleteMac(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        List<Integer> userMacs = new ArrayList<>();
        for (Integer id : ids) {
            userMacs.add(id);
        }
        macService.removeByIds(userMacs);
        return new BaseResult();
    }

    @ApiOperation(value="彻底删除", notes="")
    @SystemLogTag(description="彻底删除", moduleName=moduleName)
    @RequestMapping(value="/real_delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult realDelete(@RequestBody Integer id) {
        accountUserService.removeById(id);
        return new BaseResult();
    }

    @ApiOperation(value="恢复账户", notes="")
    @SystemLogTag(description="恢复账户", moduleName=moduleName)
    @RequestMapping(value="/recover", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult recover(@RequestBody Integer id) {
        AccountUser accountUser = accountUserService.getById(id);
        if(null == accountUser){
            return new BaseResult("0", "账户不存在", null);
        }
        accountUser.setIsValid(1);
        accountUserService.updateById(accountUser);

        return new BaseResult();
    }

}
