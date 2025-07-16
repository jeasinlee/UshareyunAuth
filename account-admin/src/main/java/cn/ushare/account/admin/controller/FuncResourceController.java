package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.config.LicenceCache;
import cn.ushare.account.admin.service.FuncResourceService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.dto.LicenceInfo;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.util.SecretAnnotation;
import cn.ushare.account.util.TreeNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "FuncResourceController", description = "功能资源")
@RestController
@Slf4j
@RequestMapping("/funcResource")
public class FuncResourceController {

    @Autowired
    SessionService sessionService;
    @Autowired
    FuncResourceService funcResourceService;
    @Value("${ushareyun.config.account}")
    private String isAccount;
    @Autowired
    LicenceCache licenceCache;

    /**
     * 树查询
     */
    @ApiOperation(value="树查询", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/getTree", method={RequestMethod.POST})
    public BaseResult<ArrayList<TreeNode>> getTree() throws Exception {
        LicenceInfo licenceInfo = licenceCache.getLicenceInfo();
        if(null== licenceInfo || null == licenceInfo.getIsAccount() || 1!=licenceInfo.getIsAccount()){
            isAccount = "0";
        }

        return funcResourceService.getTree("1".equals(isAccount));
    }

    /**
     * 新增
     */
//    @ApiOperation(value="新增", notes="")
//    @RequestMapping(value="/add", method={RequestMethod.POST})
//    public BaseResult<FuncResource> add(@RequestBody @Valid FuncResource funcResource) throws Exception {
//        funcResource.setIsValid(1);
//        funcResource.setCreateUserId(sessionService.getAdminId());
//        funcResource.setUpdateTime(new Date());
//        funcResourceService.save(funcResource);
//        return new BaseResult();
//    }

    /**
     * 修改
     */
//    @ApiOperation(value="修改", notes="")
//    @RequestMapping(value="/update", method={RequestMethod.POST})
//    public BaseResult<FuncResource> update(@RequestBody FuncResource funcResource) throws Exception {
//        funcResource.setUpdateUserId(sessionService.getAdminId());
//        funcResourceService.updateById(funcResource);
//        return new BaseResult("1", "成功", funcResource);
//    }

    /**
     * 删除
     */
//    @ApiOperation(value="删除", notes="")
//    @RequestMapping(value="/delete", method={RequestMethod.POST})
//    public BaseResult delete(@RequestBody Integer id) {
//        FuncResource funcResource = new FuncResource();
//        funcResource.setId(id);
//        funcResource.setIsValid(0);
//        funcResource.setUpdateUserId(sessionService.getAdminId());
//        funcResourceService.updateById(funcResource);
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
//        List<FuncResource> funcResourceList = new ArrayList<>();
//        for (Integer id : ids) {
//            FuncResource funcResource = new FuncResource();
//            funcResource.setId(id);
//            funcResource.setIsValid(0);
//            funcResourceList.add(funcResource);
//        }
//        funcResourceService.updateBatchById(funcResourceList);
//        return new BaseResult();
//    }

    /**
     * 查询详情
     */
//    @ApiOperation(value="查询详情", notes="")
//    @RequestMapping(value="/get", method={RequestMethod.POST})
//    public BaseResult<FuncResource> get(@RequestBody Integer id) {
//        FuncResource funcResource = funcResourceService.getById(id);
//        return new BaseResult("1", "成功", funcResource);
//    }

    /**
     * 分页查询
     */
//    @ApiOperation(value="查询", notes="")
//    @RequestMapping(value="/getList", method={RequestMethod.POST})
//    public BaseResult<BasePageResult<FuncResource>> getList(@RequestBody Map<String, Object> param) throws Exception {
//        Page<FuncResource> page = new Page<FuncResource>(1, 10);
//        if (param.get("page") != null) {
//            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
//        }
//        Map<String, Object> queryParams = new HashMap<>();
//        if (param.get("queryParams") != null) {
//            queryParams = (Map<String, Object>) param.get("queryParams");
//        }
//        return funcResourceService.getList(page, queryParams);
//    }

}
