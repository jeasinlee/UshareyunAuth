package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.SmsRecordService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.SmsRecord;
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
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "SmsRecordController", description = "短信记录")
@RestController
@Slf4j
@RequestMapping("/smsRecord")
public class SmsRecordController {

    private final static String moduleName = "短信记录";

    @Autowired
    SessionService sessionService;
    @Autowired
    SmsRecordService smsRecordService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    public BaseResult<SmsRecord> add(@RequestBody @Valid String smsRecordJson) throws Exception {
        SmsRecord smsRecord = JsonObjUtils.json2obj(smsRecordJson, SmsRecord.class);
        smsRecord.setIsValid(1);
        smsRecord.setUpdateTime(new Date());
        smsRecordService.save(smsRecord);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<SmsRecord> update(@RequestBody String smsRecordJson) throws Exception {
        SmsRecord smsRecord = JsonObjUtils.json2obj(smsRecordJson, SmsRecord.class);
        smsRecordService.updateById(smsRecord);
        return new BaseResult("1", "成功", smsRecord);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SystemLogTag(description="删除", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    public BaseResult delete(@RequestBody Integer id) {
        smsRecordService.removeById(id);
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
        List<Integer> records = new ArrayList<>();
        for (Integer id : ids) {
            records.add(id);
        }
        smsRecordService.removeByIds(records);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    public BaseResult<SmsRecord> get(@RequestBody String id) {
        SmsRecord smsRecord = smsRecordService.getById(id);
        return new BaseResult("1", "成功", smsRecord);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    public BaseResult<BasePageResult<SmsRecord>> getList(@RequestBody String paramJson) throws Exception {
        Map param = JsonObjUtils.json2map(paramJson);
        Page<SmsRecord> page = new Page<SmsRecord>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("result") && !"".equals(queryParams.get("result").toString())) {
                wrapper.eq("result", queryParams.get("result"));
            }
            if (null != queryParams.get("phone") && !"".equals(queryParams.get("phone").toString())) {
                wrapper.eq("phone", queryParams.get("phone"));
            }
            if (null != queryParams.get("businessType") && !"".equals(queryParams.get("businessType").toString())) {
                wrapper.eq("business_type", queryParams.get("businessType"));
            }
        }

        page = smsRecordService.getList(page, wrapper);

        return new BaseResult(page);
    }

    @ApiOperation(value="批量导出短信", notes="")
    @RequestMapping(value="/excelExportRecord", method={RequestMethod.GET})
    public void excelExportRecord() throws Exception {
        smsRecordService.excelExportRecord();
    }

}
