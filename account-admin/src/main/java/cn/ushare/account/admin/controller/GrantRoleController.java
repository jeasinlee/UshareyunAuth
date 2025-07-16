package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.GrantRoleService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.GrantRole;
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
@Api(tags = "GrantRoleController", description = "可授权角色")
@RestController
@Slf4j
@RequestMapping("/grantRole")
public class GrantRoleController {

    @Autowired
    SessionService sessionService;
    @Autowired
    GrantRoleService grantRoleService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<GrantRole> add(@RequestBody @Valid String grantRoleJson) throws Exception {
        GrantRole grantRole = JsonObjUtils.json2obj(grantRoleJson, GrantRole.class);
        grantRole.setIsValid(1);
        grantRole.setUpdateTime(new Date());
        grantRoleService.save(grantRole);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<GrantRole> update(@RequestBody String grantRoleJson) throws Exception {
        GrantRole grantRole = JsonObjUtils.json2obj(grantRoleJson, GrantRole.class);
        grantRoleService.updateById(grantRole);
        return new BaseResult("1", "成功", grantRole);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        grantRoleService.removeById(id);
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
        List<GrantRole> grantRoleList = new ArrayList<>();
        for (Integer id : ids) {
            GrantRole grantRole = new GrantRole();
            grantRole.setId(id);
            grantRole.setIsValid(0);
            grantRoleList.add(grantRole);
        }
        grantRoleService.updateBatchById(grantRoleList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<GrantRole> get(@RequestBody String id) {
        GrantRole grantRole = grantRoleService.getById(id);
        return new BaseResult("1", "成功", grantRole);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BasePageResult<GrantRole>> getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<GrantRole> page = new Page<GrantRole>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }

        page = grantRoleService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
