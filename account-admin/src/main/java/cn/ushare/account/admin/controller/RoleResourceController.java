package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.RoleResourceService;
import cn.ushare.account.admin.session.SessionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import javax.validation.Valid;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import cn.ushare.account.entity.RoleResource;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.util.JsonObjUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "RoleResourceController", description = "角色资源")
@RestController
@Slf4j
@RequestMapping("/roleResource")
public class RoleResourceController {

    @Autowired
    SessionService sessionService;
    @Autowired
    RoleResourceService roleResourceService;

    /**
     * 新增
     */
//    @ApiOperation(value="新增", notes="")
//    @RequestMapping(value="/add", method={RequestMethod.POST})
//    public BaseResult<RoleResource> add(@RequestBody @Valid RoleResource roleResource) throws Exception {
//        roleResource.setIsValid(1);
//        roleResource.setCreateUserId(sessionService.getAdminId());
//        roleResource.setUpdateTime(new Date());
//        roleResourceService.save(roleResource);
//        return new BaseResult();
//    }

    /**
     * 修改
     */
//    @ApiOperation(value="修改", notes="")
//    @RequestMapping(value="/update", method={RequestMethod.POST})
//    public BaseResult<RoleResource> update(@RequestBody RoleResource roleResource) throws Exception {
//        roleResource.setUpdateUserId(sessionService.getAdminId());
//        roleResourceService.updateById(roleResource);
//        return new BaseResult("1", "成功", roleResource);
//    }

    /**
     * 删除
     */
//    @ApiOperation(value="删除", notes="")
//    @RequestMapping(value="/delete", method={RequestMethod.POST})
//    public BaseResult delete(@RequestBody Integer id) {
//        RoleResource roleResource = new RoleResource();
//        roleResource.setId(id);
//        roleResource.setIsValid(0);
//        roleResource.setUpdateUserId(sessionService.getAdminId());
//        roleResourceService.updateById(roleResource);
//        return new BaseResult();
//    }

    /**
     * 批量删除
     */
//    @ApiOperation(value="批量删除", notes="")
//    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
//    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
//        if (ids == null || ids.length == 0) {
//            return new BaseResult();
//        }
//        List<RoleResource> roleResourceList = new ArrayList<>();
//        for (Integer id : ids) {
//            RoleResource roleResource = new RoleResource();
//            roleResource.setId(id);
//            roleResource.setIsValid(0);
//            roleResourceList.add(roleResource);
//        }
//        roleResourceService.updateBatchById(roleResourceList);
//        return new BaseResult();
//    }

    /**
     * 查询详情
     */
//    @ApiOperation(value="查询详情", notes="")
//    @RequestMapping(value="/get", method={RequestMethod.POST})
//    public BaseResult<RoleResource> get(@RequestBody Integer id) {
//        RoleResource roleResource = roleResourceService.getById(id);
//        return new BaseResult("1", "成功", roleResource);
//    }

    /**
     * 分页查询
     */
//    @ApiOperation(value="查询", notes="")
//    @RequestMapping(value="/getList", method={RequestMethod.POST})
//    public BaseResult<BasePageResult<RoleResource>> getList(@RequestBody Map<String, Object> param) throws Exception {
//        Page<RoleResource> page = new Page<RoleResource>(1, 10);
//        if (param.get("page") != null) {
//            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
//        }
//        Map<String, Object> queryParams = new HashMap<>();
//        if (param.get("queryParams") != null) {
//            queryParams = (Map<String, Object>) param.get("queryParams");
//        }
//        return roleResourceService.getList(page, queryParams);
//    }

}
