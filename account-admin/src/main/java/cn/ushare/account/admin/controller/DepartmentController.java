package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.DepartmentService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.Department;
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
@Api(tags = "DepartmentController", description = "部门管理")
@RestController
@Slf4j
@RequestMapping("/department")
public class DepartmentController {

    private final static String moduleName = "部门管理";

    @Autowired
    SessionService sessionService;
    @Autowired
    DepartmentService departmentService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<Department> add(@RequestBody @Valid String departmentJson) throws Exception {
        Department department = JsonObjUtils.json2obj(departmentJson, Department.class);
        // 带宽ID为0时，存null
        if (department.getBandwidthId() != null
                && department.getBandwidthId() == 0) {
            department.setBandwidthId(null);
        }

        // 除总部外，其他部门必须设置父级部门
        if (department.getParentId() == null) {
            return new BaseResult("0", "请选择上级部门", null);
        }

        // 名称不能重复
        QueryWrapper<Department> repeatQuery = new QueryWrapper();
        repeatQuery.eq("name", department.getName());
        repeatQuery.eq("is_valid", 1);
        Department repeatOne = departmentService.getOne(repeatQuery, false);
        if (repeatOne != null) {
            return new BaseResult("0", "已有该名称的部门", null);
        }

        department.setIsValid(1);
        department.setUpdateTime(new Date());
        departmentService.save(department);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<Department> update(@RequestBody String departmentJson) throws Exception {
        Department department = JsonObjUtils.json2obj(departmentJson, Department.class);
        departmentService.updateById(department);

        return new BaseResult<>();
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SystemLogTag(description="删除", moduleName=moduleName)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody String idJson) {
        Integer id = Integer.parseInt(idJson);
        if (id == 1) {
            return new BaseResult("0", "总部不能删除", null);
        }
        Department department = new Department();
        department.setId(id);
        department.setIsValid(0);
        departmentService.updateById(department);
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
        List<Department> departmentList = new ArrayList<>();
        for (Integer id : ids) {
            if ("1".equals(id)) {
                return new BaseResult("0", "总部不能删除", null);
            }
            Department department = new Department();
            department.setId(id);
            department.setIsValid(0);
            departmentList.add(department);
        }
        departmentService.updateBatchById(departmentList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    public BaseResult<Department> get(@RequestBody String id) {
        Department department = departmentService.getById(id);
        return new BaseResult("1", "成功", department);
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
        Page<Department> page = new Page<Department>(1, 100);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("id") && !"".equals(queryParams.get("id").toString())) {
                wrapper.eq("d.id", queryParams.get("id"));
            }
        }

        wrapper.eq("d.is_valid", 1);
        page = departmentService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
