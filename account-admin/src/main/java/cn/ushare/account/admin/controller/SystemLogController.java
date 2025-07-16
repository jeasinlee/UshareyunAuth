package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.SystemLogService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.SystemLog;
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
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "SystemLogController", description = "系统日志")
@RestController
@Slf4j
@RequestMapping("/systemLog")
public class SystemLogController {

    private final static String moduleName = "系统日志";

    @Autowired
    SessionService sessionService;
    @Autowired
    SystemLogService systemLogService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<SystemLog> add(@RequestBody @Valid String systemLogJson) throws Exception {
        SystemLog systemLog = JsonObjUtils.json2obj(systemLogJson, SystemLog.class);
        systemLog.setIsValid(1);
        systemLog.setUpdateTime(new Date());
        systemLogService.save(systemLog);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<SystemLog> update(@RequestBody String systemLogJson) throws Exception {
        SystemLog systemLog = JsonObjUtils.json2obj(systemLogJson, SystemLog.class);
        systemLogService.updateById(systemLog);
        return new BaseResult("1", "成功", systemLog);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SystemLogTag(description="删除", moduleName=moduleName)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        systemLogService.removeById(id);
        return new BaseResult();
    }

    /**
     * 批量删除
     */
    @ApiOperation(value="批量删除", notes="")
    @SystemLogTag(description="批量删除", moduleName=moduleName)
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        systemLogService.removeByIds(Arrays.asList(ids));
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<SystemLog> get(@RequestBody String id) {
        SystemLog systemLog = systemLogService.getById(id);
        return new BaseResult("1", "成功", systemLog);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BasePageResult<SystemLog>> getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<SystemLog> page = new Page<SystemLog>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");

            if (queryParams.containsKey("level") && null != queryParams.get("level")) {
                wrapper.eq("level", queryParams.get("level"));
            }

            if (queryParams.containsKey("startTime") && null != queryParams.get("startTime")) {
                wrapper.gt("create_time", queryParams.get("startTime"));
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                wrapper.gt("create_time", dateFormat.format(new Date()) + " 00:00:00");
            }
        }

        wrapper.eq("is_valid", 1);

        page = systemLogService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
