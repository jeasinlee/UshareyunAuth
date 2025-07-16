package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.DeviceBrandService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.DeviceBrand;
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
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "DeviceBrandController", description = "设备品牌")
@RestController
@Slf4j
@RequestMapping("/deviceBrand")
public class DeviceBrandController {

    @Autowired
    SessionService sessionService;
    @Autowired
    DeviceBrandService deviceBrandService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    public BaseResult<DeviceBrand> add(@RequestBody @Valid String deviceBrandJson) throws Exception {
        DeviceBrand deviceBrand = JsonObjUtils.json2obj(deviceBrandJson, DeviceBrand.class);
        deviceBrand.setIsValid(1);
        deviceBrand.setUpdateTime(new Date());
        deviceBrandService.save(deviceBrand);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<DeviceBrand> update(@RequestBody String deviceBrandJson) throws Exception {
        DeviceBrand deviceBrand = JsonObjUtils.json2obj(deviceBrandJson, DeviceBrand.class);
        deviceBrandService.updateById(deviceBrand);
        return new BaseResult("1", "成功", deviceBrand);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    public BaseResult delete(@RequestBody Integer id) {
        DeviceBrand deviceBrand = new DeviceBrand();
        deviceBrand.setId(id);
        deviceBrand.setIsValid(0);
        deviceBrandService.updateById(deviceBrand);
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
        List<DeviceBrand> deviceBrandList = new ArrayList<>();
        for (Integer id : ids) {
            DeviceBrand deviceBrand = new DeviceBrand();
            deviceBrand.setId(id);
            deviceBrand.setIsValid(0);
            deviceBrandList.add(deviceBrand);
        }
        deviceBrandService.updateBatchById(deviceBrandList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    public BaseResult<DeviceBrand> get(@RequestBody String id) {
        DeviceBrand deviceBrand = deviceBrandService.getById(id);
        return new BaseResult("1", "成功", deviceBrand);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    public BaseResult getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<DeviceBrand> page = new Page<DeviceBrand>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }
        page = deviceBrandService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
