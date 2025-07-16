package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.AuthRecordService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.AccountUser;
import cn.ushare.account.entity.AuthRecord;
import cn.ushare.account.entity.BaseResult;
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
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "AuthRecordController", description = "认证记录")
@RestController
@Slf4j
@RequestMapping("/authRecord")
public class AuthRecordController {

    private final static String moduleName = "认证记录";

    @Autowired
    SessionService sessionService;
    @Autowired
    AuthRecordService authRecordService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    public BaseResult<AuthRecord> add(@RequestBody @Valid String authRecordJson) throws Exception {
        AuthRecord authRecord = JsonObjUtils.json2obj(authRecordJson, AuthRecord.class);
        authRecord.setIsValid(1);
        authRecord.setUpdateTime(new Date());
        authRecordService.save(authRecord);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<AuthRecord> update(@RequestBody String authRecordJson) throws Exception {
        AuthRecord authRecord = JsonObjUtils.json2obj(authRecordJson, AuthRecord.class);
        authRecordService.updateById(authRecord);
        return new BaseResult("1", "成功", authRecord);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    public BaseResult delete(@RequestBody Long id) {
        authRecordService.removeById(id);
        return new BaseResult();
    }

    /**
     * 批量删除
     */
    @ApiOperation(value="批量删除", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    public BaseResult batchDelete(@RequestBody Long[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }

        authRecordService.removeByIds(Arrays.asList(ids));
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<AuthRecord> get(@RequestBody String id) {
        AuthRecord authRecord = authRecordService.getById(id);
        return new BaseResult("1", "成功", authRecord);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<AuthRecord> page = new Page<AuthRecord>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper<AuthRecord> wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");

            if (queryParams.containsKey("ip") && null != queryParams.get("ip")) {
                wrapper.eq("ip", queryParams.get("ip"));
            }
            if (queryParams.containsKey("mac") && null != queryParams.get("mac")) {
                wrapper.eq("mac", queryParams.get("mac"));
            }
            if (queryParams.containsKey("authMethod") && null != queryParams.get("authMethod")) {
                wrapper.eq("auth_method", queryParams.get("authMethod"));
            }
            if (queryParams.containsKey("loginName") && null != queryParams.get("loginName")) {
                wrapper.and(q -> q.like("user_name", queryParams.get("loginName"))
                        .or().like("show_user_name", queryParams.get("loginName")));
            }
            if (queryParams.containsKey("userId") && null != queryParams.get("userId")) {
                wrapper.eq("user_id", queryParams.get("userId"));
            }
            if (queryParams.containsKey("phone") && null != queryParams.get("phone")) {
                wrapper.eq("phone", queryParams.get("phone"));
            }

            if (queryParams.containsKey("startTime") && null != queryParams.get("startTime")) {
                wrapper.gt("create_time", queryParams.get("startTime"));
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                wrapper.gt("create_time", dateFormat.format(new Date()) + " 00:00:00");
            }
        }

        wrapper.eq("is_valid", 1);
        page = authRecordService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
