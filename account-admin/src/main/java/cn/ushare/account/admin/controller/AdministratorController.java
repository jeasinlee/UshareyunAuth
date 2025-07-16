package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.AdministratorService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.dto.AdminChangePasswordReq;
import cn.ushare.account.dto.AdminForgetPasswordReq;
import cn.ushare.account.dto.AdminLoginReq;
import cn.ushare.account.dto.AdminSendSmsReq;
import cn.ushare.account.entity.Administrator;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.ImageCode;
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

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.*;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "AdministratorController", description = "管理员账户")
@RestController
@Slf4j
@RequestMapping("/administrator")
public class AdministratorController {

    private final static String moduleName = "管理员账户";

    @Autowired
    HttpServletRequest request;
    @Autowired
    HttpServletResponse response;
    @Autowired
    SessionService sessionService;
    @Autowired
    AdministratorService administratorService;

    /**
     * 登录
     */
    @ApiOperation(value="登录", notes="")
    @RequestMapping(value="/login", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="登录", moduleName=moduleName)
    public BaseResult login(@RequestBody AdminLoginReq adminLogin) throws Exception {
        return administratorService.login(adminLogin);
    }

    /**
     * 退出
     */
    @ApiOperation(value="退出", notes="")
    @RequestMapping(value="/logout", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="退出", moduleName=moduleName)
    public BaseResult<Administrator> logout() throws Exception {
        return administratorService.logout();
    }

    /**
     * 修改密码
     */
    @ApiOperation(value="修改密码", notes="")
    @RequestMapping(value="/changePassword", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="修改密码", moduleName=moduleName)
    public BaseResult<Administrator> changePassword(@RequestBody AdminChangePasswordReq param) throws Exception {
        return administratorService.changePassword(param);
    }

    /**
     * 忘记密码
     */
    @ApiOperation(value="忘记密码", notes="")
    @RequestMapping(value="/forgetPassword", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="忘记密码", moduleName=moduleName)
    public BaseResult<Administrator> forgetPassword(@RequestBody AdminForgetPasswordReq param) throws Exception {
        return administratorService.forgetPassword(param);
    }

    /**
     * 发送短信验证码
     */
    @ApiOperation(value="发送短信验证码", notes="")
    @SecretAnnotation(encode = true, decode = true)
    @RequestMapping(value="/sendSmsCode", method={RequestMethod.POST})
    public BaseResult sendSmsCode(@RequestBody AdminSendSmsReq param) throws Exception {
        return administratorService.sendSmsCode(param);
    }

    /**
     * 图片验证码
     */
    @ApiOperation(value="图片验证码", notes="")
    @RequestMapping(value = "/checkCode", method={RequestMethod.GET})
    public String checkCode() throws Exception {
        OutputStream os = response.getOutputStream();
        Map<String, Object> imageMap = ImageCode.getImageCode(50, 20, os);
        String checkCode = imageMap.get("strEnsure").toString().toLowerCase();
        request.getSession().setAttribute("checkCode", checkCode);
        ImageIO.write((BufferedImage) imageMap.get("image"), "JPEG", os);
        return "ok";
    }

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="新增", moduleName=moduleName)
    public BaseResult<Administrator> add(@RequestBody @Valid Administrator administrator) throws Exception {
        // 账号是否重复
        QueryWrapper<Administrator> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", administrator.getUserName());
        queryWrapper.eq("is_valid", 1);
        Administrator repeatOne = administratorService.getOne(queryWrapper, false);
        if (repeatOne != null) {
            return new BaseResult("0", "已有相同账号", null);
        }

        // 手机是否重复
        QueryWrapper<Administrator> phoneQuery = new QueryWrapper();
        phoneQuery.eq("phone", administrator.getPhone());
        phoneQuery.eq("is_valid", 1);
        Administrator repeatPhone = administratorService.getOne(phoneQuery, false);
        if (repeatPhone != null) {
            return new BaseResult("0", "已有相同手机号码", null);
        }

        administrator.setIsValid(1);
        administrator.setUpdateTime(new Date());
        administratorService.save(administrator);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="修改", moduleName=moduleName)
    public BaseResult<Administrator> update(@RequestBody Administrator administrator) throws Exception {
        // 账号是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", administrator.getUserName());
        queryWrapper.eq("is_valid", 1);
        Administrator repeatOne = administratorService.getOne(queryWrapper);
        if (repeatOne != null && repeatOne.getId() != administrator.getId()) {
            return new BaseResult("0", "已有相同账号", null);
        }

        // 手机是否重复
        QueryWrapper<Administrator> phoneQuery = new QueryWrapper();
        phoneQuery.eq("phone", administrator.getPhone());
        phoneQuery.eq("is_valid", 1);
        Administrator repeatPhone = administratorService.getOne(phoneQuery);
        if (repeatPhone != null && repeatPhone.getId() != administrator.getId()) {
            return new BaseResult("0", "已有相同手机号码", null);
        }

        administratorService.updateById(administrator);

        return new BaseResult("1", "成功", administrator);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="删除", moduleName=moduleName)
    public BaseResult delete(@RequestBody Integer id) {
        if (id == 1) {
            return new BaseResult("0", "不允许删除超级管理员", null);
        }
        Administrator administrator = new Administrator();
        administrator.setId(id);
        administrator.setIsValid(0);
        administratorService.updateById(administrator);
        return new BaseResult();
    }

    /**
     * 批量删除
     */
    @ApiOperation(value="批量删除", notes="")
    @RequestMapping(value="/batchDelete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="批量删除", moduleName=moduleName)
    public BaseResult batchDelete(@RequestBody Integer[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            return new BaseResult();
        }
        List<Administrator> administratorList = new ArrayList<>();
        for (Integer id : ids) {
            if (id == 1) {
                return new BaseResult("0", "不允许删除超级管理员", null);
            }
            Administrator administrator = new Administrator();
            administrator.setId(id);
            administrator.setIsValid(0);
            administratorList.add(administrator);
        }
        administratorService.updateBatchById(administratorList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    @SystemLogTag(description="查询", moduleName=moduleName)
    public BaseResult<Administrator> get(@RequestBody String id) {
        Administrator administrator = administratorService.getById(id);
        return new BaseResult("1", "成功", administrator);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BasePageResult<Administrator>> getList(@RequestBody Map<String, Object> param) throws Exception {
        Page<Administrator> page = new Page<Administrator>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper<Administrator> wrapper = new QueryWrapper();

        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");
            if (null != queryParams.get("fullName") && !"".equals(queryParams.get("fullName").toString())) {
                String fullName = queryParams.get("fullName").toString();
                wrapper.and( q -> q.like("a.full_name", fullName).or().like("a.user_name", fullName));
            }
        }
        page = administratorService.getList(page, wrapper);

        return new BaseResult(page);
    }

}
