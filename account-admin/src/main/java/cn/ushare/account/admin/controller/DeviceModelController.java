package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.DeviceModelService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.DeviceModel;
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
@Api(tags = "DeviceModelController", description = "设备型号")
@RestController
@Slf4j
@RequestMapping("/deviceModel")
public class DeviceModelController {

    @Autowired
    SessionService sessionService;
    @Autowired
    DeviceModelService deviceModelService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DeviceModel> add(@RequestBody @Valid String deviceModelJson) throws Exception {
        DeviceModel deviceModel = JsonObjUtils.json2obj(deviceModelJson, DeviceModel.class);
        deviceModel.setIsValid(1);
        deviceModel.setUpdateTime(new Date());
        deviceModelService.save(deviceModel);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DeviceModel> update(@RequestBody String deviceModelJson) throws Exception {
        DeviceModel deviceModel = JsonObjUtils.json2obj(deviceModelJson, DeviceModel.class);
        deviceModelService.updateById(deviceModel);
        return new BaseResult("1", "成功", deviceModel);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        DeviceModel deviceModel = new DeviceModel();
        deviceModel.setId(id);
        deviceModel.setIsValid(0);
        deviceModelService.updateById(deviceModel);
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
        List<DeviceModel> deviceModelList = new ArrayList<>();
        for (Integer id : ids) {
            DeviceModel deviceModel = new DeviceModel();
            deviceModel.setId(id);
            deviceModel.setIsValid(0);
            deviceModelList.add(deviceModel);
        }
        deviceModelService.updateBatchById(deviceModelList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DeviceModel> get(@RequestBody String id) {
        DeviceModel deviceModel = deviceModelService.getById(id);
        return new BaseResult("1", "成功", deviceModel);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<DeviceModel> page = new Page<DeviceModel>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }
        page = deviceModelService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
