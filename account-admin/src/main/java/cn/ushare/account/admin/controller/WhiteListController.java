package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.WhiteListService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.WhiteList;
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
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "WhiteListController", description = "白名单")
@RestController
@Slf4j
@RequestMapping("/whiteList")
public class WhiteListController {

    private final static String moduleName = "白名单";

    @Autowired
    SessionService sessionService;
    @Autowired
    WhiteListService whiteListService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<WhiteList> add(@RequestBody @Valid String whiteListJson) throws Exception {
        WhiteList whiteList = JsonObjUtils.json2obj(whiteListJson, WhiteList.class);
        return whiteListService.add(whiteList);
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<WhiteList> update(@RequestBody String whiteListJson) throws Exception {
        WhiteList whiteList = JsonObjUtils.json2obj(whiteListJson, WhiteList.class);
        return whiteListService.update(whiteList);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SystemLogTag(description="删除", moduleName=moduleName)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        whiteListService.removeById(id);
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
        List<Integer> whiteListList = new ArrayList<>();
        for (Integer id : ids) {
            whiteListList.add(id);
        }
        whiteListService.removeByIds(whiteListList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<WhiteList> get(@RequestBody String id) {
        WhiteList whiteList = whiteListService.getById(id);
        return new BaseResult("1", "成功", whiteList);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BasePageResult<WhiteList>> getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<WhiteList> page = new Page<WhiteList>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper<WhiteList> wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("type") && !"".equals(queryParams.get("type").toString())) {
                wrapper.eq("type", queryParams.get("type"));
            }
            if (null != queryParams.get("value") && !"".equals(queryParams.get("value").toString())) {
                wrapper.like("value", queryParams.get("value"));
            }

            if (null != queryParams.get("nickName") && !"".equals(queryParams.get("nickName").toString())) {
                wrapper.like("user_name", queryParams.get("nickName"));
            }
        }
        page = whiteListService.getList(page, wrapper);

        return new BaseResult(page);
    }

    /**
     * 手机白名单模板导入
     */
    @ApiOperation(value="手机白名单模板导入", notes="")
    @SystemLogTag(description="手机白名单模板导入", moduleName=moduleName)
    @RequestMapping(value="/excelImportPhone", method={RequestMethod.POST})
    public BaseResult excelImportPhone(MultipartFile file) throws Exception {
        return whiteListService.excelImportPhone(file);
    }

    /**
     * MAC白名单模板导入
     */
    @ApiOperation(value="MAC白名单模板导入", notes="")
    @SystemLogTag(description="MAC白名单模板导入", moduleName=moduleName)
    @RequestMapping(value="/excelImportMac", method={RequestMethod.POST})
    public BaseResult excelImportMac(MultipartFile file) throws Exception {
        return whiteListService.excelImportMac(file);
    }

    /**
     * 模板导出手机
     */
    @ApiOperation(value="模板导出手机", notes="")
    @RequestMapping(value="/excelExportPhone", method={RequestMethod.GET})
    public void excelExportPhone(String ids) throws Exception {
        whiteListService.excelExportPhone(ids);
    }

    /**
     * 模板导出MAC
     */
    @ApiOperation(value="模板导出MAC", notes="")
    @RequestMapping(value="/excelExportMac", method={RequestMethod.GET})
    public void excelExportMac(String ids) throws Exception {
        whiteListService.excelExportMac(ids);
    }

}
