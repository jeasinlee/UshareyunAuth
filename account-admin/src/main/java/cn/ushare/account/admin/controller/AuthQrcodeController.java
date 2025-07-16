package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.AuthQrcodeService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.AuthQrcode;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.SecretAnnotation;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "AuthQrcodeController", description = "认证二维码")
@RestController
@Slf4j
@RequestMapping("/authQrcode")
public class AuthQrcodeController {

    private final static String moduleName = "认证二维码";

    @Autowired
    SessionService sessionService;
    @Autowired
    AuthQrcodeService authQrcodeService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    public BaseResult<AuthQrcode> add(@RequestBody @Valid String authQrcodeJson) throws Exception {
        AuthQrcode authQrcode = JsonObjUtils.json2obj(authQrcodeJson, AuthQrcode.class);
        if (authQrcode.getAcId() == null) {
            return new BaseResult("0", "请选择AC控制器", null);
        }
        return authQrcodeService.add(authQrcode);
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    public BaseResult<AuthQrcode> update(@RequestBody String authQrcodeJson) throws Exception {
        AuthQrcode authQrcode = JsonObjUtils.json2obj(authQrcodeJson, AuthQrcode.class);
        authQrcodeService.updateById(authQrcode);

        // 如果设为默认，则其他二维码取消默认
        if (authQrcode.getIsDefault() == 1) {
            setDefault(JsonObjUtils.obj2json(authQrcode));
        }

        return new BaseResult("1", "成功", authQrcode);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SystemLogTag(description="删除", moduleName=moduleName)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody String id) {
        return authQrcodeService.delete(Integer.parseInt(id));
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
        List<Integer> records = new ArrayList<>();
        for (Integer id : ids) {
            authQrcodeService.delete(id);
            records.add(id);
        }
        authQrcodeService.removeByIds(records);
        return new BaseResult();
    }

    /**
     * 设置默认
     */
    @ApiOperation(value="设置默认", notes="")
    @SystemLogTag(description="设置默认", moduleName=moduleName)
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/setDefault", method={RequestMethod.POST})
    public BaseResult<AuthQrcode> setDefault(@RequestBody String authQrcodeJson) throws Exception {
        AuthQrcode authQrcode = JsonObjUtils.json2obj(authQrcodeJson, AuthQrcode.class);
        QueryWrapper<AuthQrcode> query = new QueryWrapper();
        query.eq("ac_id", authQrcode.getAcId());
        query.eq("is_valid", 1);
        List<AuthQrcode> list = authQrcodeService.list(query);
        // 同一个ac下，选中的这个置1，其他置0
        for (int i = 0; i < list.size(); i++) {
            AuthQrcode item = list.get(i);
            if (item.getId().equals(authQrcode.getId())) {
                item.setIsDefault(1);
            } else {
                item.setIsDefault(0);
            }
            authQrcodeService.updateById(item);
        }

        return new BaseResult("1", "成功", authQrcode);
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<AuthQrcode> get(@RequestBody String id) {
        AuthQrcode authQrcode = authQrcodeService.getById(id);
        return new BaseResult("1", "成功", authQrcode);
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
        Page<AuthQrcode> page = new Page<AuthQrcode>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
        }

        page = authQrcodeService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
