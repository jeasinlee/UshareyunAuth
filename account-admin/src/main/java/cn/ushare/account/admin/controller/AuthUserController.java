package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.AuthUserService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.AuthUser;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.SecretAnnotation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;

/**
 * @author jixiang.li
 * @date 2019-05-03
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "AuthUserController", description = "认证用户")
@RestController
@Slf4j
@RequestMapping("/authUser")
public class AuthUserController {

    private final static String moduleName = "认证用户";

    @Autowired
    SessionService sessionService;
    @Autowired
    AuthUserService authUserService;

    /**
     * 强制下线
     */
    @ApiOperation(value="强制下线", notes="")
    @SystemLogTag(description="强制下线", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/offline", method={RequestMethod.POST})
    public BaseResult<AuthUser> offline(@RequestBody Long[] id) throws Exception {
        return authUserService.offline(id[0]);
    }

    /**
     * 批量强制下线
     */
    @ApiOperation(value="批量强制下线", notes="")
    @SystemLogTag(description="批量强制下线", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/batchOffline", method={RequestMethod.POST})
    public BaseResult<AuthUser> batchOffline(@RequestBody Long[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        for (Long id  : ids) {
            BaseResult result = authUserService.offline(id);
            if (result.getReturnCode().equals("0")) {
                return result;
            }
        }
        return new BaseResult();
    }

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    public BaseResult<AuthUser> add(@RequestBody @Valid String authUserJson) throws Exception {
        AuthUser authUser = JsonObjUtils.json2obj(authUserJson, AuthUser.class);
        authUser.setIsValid(1);
        authUser.setUpdateTime(new Date());
        authUserService.save(authUser);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<AuthUser> update(@RequestBody String authUserJson) throws Exception {
        AuthUser authUser = JsonObjUtils.json2obj(authUserJson, AuthUser.class);
        authUserService.updateById(authUser);
        return new BaseResult("1", "成功", authUser);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SystemLogTag(description="删除", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    public BaseResult delete(@RequestBody Integer id) {
        AuthUser authUser = new AuthUser();
        authUser.setId(id);
        authUser.setIsValid(0);
        authUserService.updateById(authUser);
        return new BaseResult();
    }

    /**
     * 批量删除
     */
    @ApiOperation(value="批量删除", notes="")
    @SystemLogTag(description="批量删除", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        List<AuthUser> authUserList = new ArrayList<>();
        for (Integer id : ids) {
            AuthUser authUser = new AuthUser();
            authUser.setId(id);
            authUser.setIsValid(0);
            authUserList.add(authUser);
        }
        authUserService.updateBatchById(authUserList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    public BaseResult<AuthUser> get(@RequestBody String id) {
        AuthUser authUser = authUserService.getById(id);
        return new BaseResult("1", "成功", authUser);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    public BaseResult getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<AuthUser> page = new Page<AuthUser>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper<AuthUser> wrapper = new QueryWrapper();
        wrapper.eq("online_state", 1);
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");

            if (queryParams.containsKey("ip") && null != queryParams.get("ip")) {
                wrapper.eq("ip", queryParams.get("ip"));
            }
            if (queryParams.containsKey("ssid") && null != queryParams.get("ssid")) {
                wrapper.eq("ssid", queryParams.get("ssid"));
            }
            if (queryParams.containsKey("mac") && null != queryParams.get("mac")) {
                wrapper.eq("mac", queryParams.get("mac"));
            }
            if (queryParams.containsKey("userName") && null != queryParams.get("userName")) {
                wrapper.like("user_name",queryParams.get("userName")).or().like("show_user_name", queryParams.get("userName"));
            }
            if (queryParams.containsKey("authMethod") && null != queryParams.get("authMethod")) {
                wrapper.eq("auth_method", queryParams.get("authMethod"));
            }
        }
        wrapper.orderByDesc("create_time");
        page = authUserService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
