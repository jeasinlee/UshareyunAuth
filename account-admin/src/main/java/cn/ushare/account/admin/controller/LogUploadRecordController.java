package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.LogUploadRecordService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.LogUploadRecord;
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
 * @date 2019-04-29
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "LogUploadRecordController", description = "")
@RestController
@Slf4j
@RequestMapping("/logUploadRecord")
public class LogUploadRecordController {

    private final static String moduleName = "日志上传记录";

    @Autowired
    SessionService sessionService;
    @Autowired
    LogUploadRecordService logUploadRecordService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    public BaseResult<LogUploadRecord> add(@RequestBody @Valid String logUploadRecordJson) throws Exception {
        LogUploadRecord logUploadRecord = JsonObjUtils.json2obj(logUploadRecordJson, LogUploadRecord.class);
        logUploadRecord.setIsValid(1);
        logUploadRecord.setUpdateTime(new Date());
        logUploadRecordService.save(logUploadRecord);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<LogUploadRecord> update(@RequestBody String logUploadRecordJson) throws Exception {
        LogUploadRecord logUploadRecord = JsonObjUtils.json2obj(logUploadRecordJson, LogUploadRecord.class);
        logUploadRecordService.updateById(logUploadRecord);
        return new BaseResult("1", "成功", logUploadRecord);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        logUploadRecordService.removeById(id);
        return new BaseResult();
    }

    /**
     * 批量删除
     */
    @ApiOperation(value="批量删除", notes="")
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        List<LogUploadRecord> logUploadRecordList = new ArrayList<>();
        for (Integer id : ids) {
            LogUploadRecord logUploadRecord = new LogUploadRecord();
            logUploadRecord.setId(id);
            logUploadRecord.setIsValid(0);
            logUploadRecordList.add(logUploadRecord);
        }
        logUploadRecordService.updateBatchById(logUploadRecordList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<LogUploadRecord> get(@RequestBody String id) {
        LogUploadRecord logUploadRecord = logUploadRecordService.getById(id);
        return new BaseResult("1", "成功", logUploadRecord);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BasePageResult<LogUploadRecord>> getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<LogUploadRecord> page = new Page<LogUploadRecord>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }

        QueryWrapper wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }

        page = logUploadRecordService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
