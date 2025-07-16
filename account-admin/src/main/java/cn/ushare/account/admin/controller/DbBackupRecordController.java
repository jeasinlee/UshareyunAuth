package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.DbBackupRecordService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.DbBackupRecord;
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
 * @date 2019-04-30
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "DbBackupRecordController", description = "数据库备份")
@RestController
@Slf4j
@RequestMapping("/dbBackupRecord")
public class DbBackupRecordController {

    private final static String moduleName = "数据库备份";

    @Autowired
    SessionService sessionService;
    @Autowired
    DbBackupRecordService dbBackupRecordService;

    /**
     * 本地备份
     */
    @ApiOperation(value="本地备份", notes="")
    @SystemLogTag(description="本地备份", moduleName=moduleName)
    @RequestMapping(value="/localBackup", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DbBackupRecord> localBackup() throws Exception {
        return dbBackupRecordService.localBackup();
    }

    /**
     * 本地还原
     */
    @ApiOperation(value="本地还原", notes="")
    @SystemLogTag(description="本地还原", moduleName=moduleName)
    @RequestMapping(value="/localRestore", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DbBackupRecord> localRestore(@RequestBody String fileName) throws Exception {
        return dbBackupRecordService.localRestore(fileName);
    }

    /**
     * 数据库云备份
     */
    @ApiOperation(value="数据库云备份", notes="")
    @SystemLogTag(description="云备份", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/cloudBackup", method={RequestMethod.POST})
    public BaseResult cloudBackup() throws Exception {
        return dbBackupRecordService.cloudBackup();
    }

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DbBackupRecord> add(@RequestBody @Valid String dbBackupRecordJson) throws Exception {
        DbBackupRecord dbBackupRecord = JsonObjUtils.json2obj(dbBackupRecordJson, DbBackupRecord.class);
        dbBackupRecord.setIsValid(1);
        dbBackupRecord.setUpdateTime(new Date());
        dbBackupRecordService.save(dbBackupRecord);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DbBackupRecord> update(@RequestBody String dbBackupRecordJson) throws Exception {
        DbBackupRecord dbBackupRecord = JsonObjUtils.json2obj(dbBackupRecordJson, DbBackupRecord.class);
        dbBackupRecordService.updateById(dbBackupRecord);
        return new BaseResult("1", "成功", dbBackupRecord);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SystemLogTag(description="删除", moduleName=moduleName)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody String id) {
        DbBackupRecord dbBackupRecord = new DbBackupRecord();
        dbBackupRecord.setId(Integer.parseInt(id));
        dbBackupRecord.setIsValid(0);
        dbBackupRecordService.updateById(dbBackupRecord);
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
        List<DbBackupRecord> dbBackupRecordList = new ArrayList<>();
        for (Integer id : ids) {
            DbBackupRecord dbBackupRecord = new DbBackupRecord();
            dbBackupRecord.setId(id);
            dbBackupRecord.setIsValid(0);
            dbBackupRecordList.add(dbBackupRecord);
        }
        dbBackupRecordService.updateBatchById(dbBackupRecordList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DbBackupRecord> get(@RequestBody String id) {
        DbBackupRecord dbBackupRecord = dbBackupRecordService.getById(id);
        return new BaseResult("1", "成功", dbBackupRecord);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<DbBackupRecord> page = new Page<DbBackupRecord>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }
        page = dbBackupRecordService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
