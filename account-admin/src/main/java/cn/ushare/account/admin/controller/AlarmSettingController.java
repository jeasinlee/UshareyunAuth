package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.AlarmSettingService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.AlarmSetting;
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
 * @since 2019-03-30
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "AlarmSettingController", description = "警报设置")
@RestController
@Slf4j
@RequestMapping("/alarmSetting")
public class AlarmSettingController {

    private final static String moduleName = "警报设置";

    @Autowired
    SessionService sessionService;
    @Autowired
    AlarmSettingService alarmSettingService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    public BaseResult<AlarmSetting> add(@RequestBody @Valid AlarmSetting alarmSetting) throws Exception {
        alarmSetting.setIsValid(1);
        alarmSetting.setUpdateTime(new Date());
        alarmSettingService.save(alarmSetting);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<AlarmSetting> update(@RequestBody List<AlarmSetting> list) throws Exception {
        for (int i = 0; i < list.size(); i++) {
            AlarmSetting item = list.get(i);
            alarmSettingService.updateById(item);
        }
        return new BaseResult("1", "成功", list);
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="批量修改", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/updateList", method={RequestMethod.POST})
    public BaseResult<AlarmSetting> updateList(@RequestBody String paramJson) throws Exception {
        Map<String, Map> param = (Map<String, Map>) JsonObjUtils.json2map(paramJson);
        Map<String, Object> cpuMap = param.get("cpuConfig");
        AlarmSetting cpuSetting = new AlarmSetting();
        cpuSetting.setId((Integer) cpuMap.get("id"));
        cpuSetting.setType((Integer) cpuMap.get("type"));
        cpuSetting.setIsCustom((Integer) cpuMap.get("isCustom"));
        cpuSetting.setThreshold((String) cpuMap.get("threshold"));
        cpuSetting.setStatus((Integer) cpuMap.get("status"));
        alarmSettingService.updateById(cpuSetting);

        Map<String, Object> memMap = param.get("memConfig");
        AlarmSetting memSetting = new AlarmSetting();
        memSetting.setId((Integer) memMap.get("id"));
        memSetting.setType((Integer) memMap.get("type"));
        memSetting.setIsCustom((Integer) memMap.get("isCustom"));
        memSetting.setThreshold((String) memMap.get("threshold"));
        memSetting.setStatus((Integer) memMap.get("status"));
        alarmSettingService.updateById(memSetting);

        Map<String, Object> hwMap = param.get("hwConfig");
        AlarmSetting hwSetting = new AlarmSetting();
        hwSetting.setId((Integer) hwMap.get("id"));
        hwSetting.setType((Integer) hwMap.get("type"));
        hwSetting.setIsCustom((Integer) hwMap.get("isCustom"));
        hwSetting.setThreshold((String) hwMap.get("threshold"));
        hwSetting.setStatus((Integer) hwMap.get("status"));
        alarmSettingService.updateById(hwSetting);

        Map<String, Object> userNumMap = param.get("userNumConfig");
        AlarmSetting userNumSetting = new AlarmSetting();
        userNumSetting.setId((Integer) userNumMap.get("id"));
        userNumSetting.setType((Integer) userNumMap.get("type"));
        userNumSetting.setIsCustom((Integer) userNumMap.get("isCustom"));
        userNumSetting.setThreshold((String) userNumMap.get("threshold"));
        userNumSetting.setStatus((Integer) userNumMap.get("status"));
        alarmSettingService.updateById(userNumSetting);

        return new BaseResult("1", "成功", null);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    public BaseResult delete(@RequestBody Integer id) {
        AlarmSetting alarmSetting = new AlarmSetting();
        alarmSetting.setId(id);
        alarmSetting.setIsValid(0);
        alarmSettingService.updateById(alarmSetting);
        return new BaseResult();
    }

    /**
     * 批量删除
     */
    @ApiOperation(value="批量删除", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        List<AlarmSetting> alarmSettingList = new ArrayList<>();
        for (Integer id : ids) {
            AlarmSetting alarmSetting = new AlarmSetting();
            alarmSetting.setId(id);
            alarmSetting.setIsValid(0);
            alarmSettingList.add(alarmSetting);
        }
        alarmSettingService.updateBatchById(alarmSettingList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    public BaseResult<AlarmSetting> get(@RequestBody String id) {
        AlarmSetting alarmSetting = alarmSettingService.getById(id);
        return new BaseResult("1", "成功", alarmSetting);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询详情", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    public BaseResult getList(@RequestBody String paramJson) throws Exception {
        Map param  = JsonObjUtils.json2map(paramJson);
        Page<AlarmSetting> page = new Page<AlarmSetting>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }

        page = alarmSettingService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
