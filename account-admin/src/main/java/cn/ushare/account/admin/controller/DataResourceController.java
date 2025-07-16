package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.DataResourceService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.DataResource;
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
@Api(tags = "DataResourceController", description = "数据资源")
@RestController
@Slf4j
@RequestMapping("/dataResource")
public class DataResourceController {

    @Autowired
    SessionService sessionService;
    @Autowired
    DataResourceService dataResourceService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DataResource> add(@RequestBody @Valid String dataResourceJson) throws Exception {
        DataResource dataResource = JsonObjUtils.json2obj(dataResourceJson, DataResource.class);
        dataResource.setIsValid(1);
        dataResource.setUpdateTime(new Date());
        dataResourceService.save(dataResource);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DataResource> update(@RequestBody String dataResourceJson) throws Exception {
        DataResource dataResource = JsonObjUtils.json2obj(dataResourceJson, DataResource.class);
        dataResourceService.updateById(dataResource);
        return new BaseResult("1", "成功", dataResource);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        dataResourceService.removeById(id);
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
        List<DataResource> dataResourceList = new ArrayList<>();
        for (Integer id : ids) {
            DataResource dataResource = new DataResource();
            dataResource.setId(id);
            dataResource.setIsValid(0);
            dataResourceList.add(dataResource);
        }
        dataResourceService.updateBatchById(dataResourceList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<DataResource> get(@RequestBody String id) {
        DataResource dataResource = dataResourceService.getById(id);
        return new BaseResult("1", "成功", dataResource);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<DataResource> page = new Page<DataResource>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }
        page = dataResourceService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
