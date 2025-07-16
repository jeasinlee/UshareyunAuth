package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.portal.service.PortalUtil;
import cn.ushare.account.admin.service.DepartmentService;
import cn.ushare.account.admin.service.EmployeeService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.dto.EmployeeChangePwdReq;
import cn.ushare.account.dto.EmployeeFirstModifyPwdReq;
import cn.ushare.account.dto.EmployeeGetSmsReq;
import cn.ushare.account.dto.LoginGetSmsReq;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.Employee;
import cn.ushare.account.log.SystemLogTag;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.SecretAnnotation;
import cn.ushare.account.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.*;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Api(tags = "EmployeeController", description = "员工")
@RestController
@Slf4j
@RequestMapping("/employee")
public class EmployeeController {

    private final static String moduleName = "员工管理";

    @Autowired
    SessionService sessionService;
    @Autowired
    EmployeeService employeeService;

    @Autowired
    DepartmentService departmentService;

    /**
     * 新增
     */
    @ApiOperation(value="新增", notes="")
    @SystemLogTag(description="新增", moduleName=moduleName)
    @RequestMapping(value="/add", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<Employee> add(@RequestBody @Valid String employeeJson) throws Exception {
        Employee employee = JsonObjUtils.json2obj(employeeJson, Employee.class);
        if (employee.getUserName() == null) {
            return new BaseResult("0", "用户名不能为空", null);
        }

        // 检查账号是否重复
        QueryWrapper<Employee> userNameQuery = new QueryWrapper();
        userNameQuery.eq("user_name", employee.getUserName());
        userNameQuery.eq("is_valid", 1);
        List<Employee> list = employeeService.list(userNameQuery);
        if (list.size() > 0) {
            return new BaseResult("0", "账号不能重复", null);
        }

        // 检查手机是否重复
        if (StringUtil.isNotBlank(employee.getPhone())) {
            QueryWrapper<Employee> phoneQuery = new QueryWrapper();
            phoneQuery.eq("phone", employee.getPhone());
            phoneQuery.eq("is_valid", 1);
            List<Employee> phoneList = employeeService.list(phoneQuery);
            if (phoneList.size() > 0) {
                return new BaseResult("0", "手机号码不能重复", null);
            }
        }

        //判断mac是否超限
        if(employee.getIsBindMac()==1){
            if (employee.getBindMacs().split(",").length > employee.getTerminalNum()) {
                return new BaseResult("0", "绑定mac超过最大限制", null);
            }
            employee.setBindMacs(PortalUtil.MacFormat1(employee.getBindMacs()));
        } else {
            employee.setBindMacs(null);
        }

        // 带宽ID为0时，存null
        if (employee.getBandwidthId() != null
                && employee.getBandwidthId() == 0) {
            employee.setBandwidthId(null);
        }

        employee.setIsValid(1);
        employee.setUpdateTime(new Date());
        employeeService.save(employee);
        return new BaseResult();
    }

    /**
     * 修改
     */
    @ApiOperation(value="修改", notes="")
    @SystemLogTag(description="修改", moduleName=moduleName)
    @RequestMapping(value="/update", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<Employee> update(@RequestBody String employeeJson) throws Exception {
        log.info("===req:" + employeeJson);
        Employee employee = JsonObjUtils.json2obj(employeeJson, Employee.class);
        // 检查账号是否重复
        Employee employeeSaved = employeeService.getById(employee.getId());
        if(null==employeeSaved){
            return new BaseResult("0", "用户不存在", null);
        }
        // 检查账号是否重复
        if (StringUtil.isNotBlank(employee.getUserName())) {
            QueryWrapper<Employee> userNameQuery = new QueryWrapper();
            userNameQuery.eq("user_name", employee.getUserName());
            userNameQuery.eq("is_valid", 1);
            List<Employee> list = employeeService.list(userNameQuery);
            if (list.size() > 0) {
                if (!list.get(0).getId().equals(employee.getId())) {
                    return new BaseResult("0", "账号不能重复", null);
                }
            }
        }

        // 检查手机是否重复
        if (StringUtil.isNotBlank(employee.getPhone())) {
            QueryWrapper<Employee> phoneQuery = new QueryWrapper();
            phoneQuery.eq("phone", employee.getPhone());
            phoneQuery.eq("is_valid", 1);
            List<Employee> phoneList = employeeService.list(phoneQuery);
            if (phoneList.size() > 0) {
                if (!phoneList.get(0).getId().equals(employee.getId())) {
                    return new BaseResult("0", "手机号码不能重复", null);
                }
            }
        }

        //判断mac是否超限
        if(null!=employee.getBindMacs()) {
            if (employee.getIsBindMac() == 1) {
                if (employee.getBindMacs().split(",").length > employee.getTerminalNum()) {
                    return new BaseResult("0", "绑定mac超过最大限制", null);
                }
                employee.setBindMacs(PortalUtil.MacFormat1(employee.getBindMacs()));
            }
        }

        // 带宽ID为0时，存null
        if (employee.getBandwidthId() != null
                && employee.getBandwidthId() == 0) {
            employee.setBandwidthId(null);
            employeeService.setBandwidthNull(employee.getId());
        }

        employeeService.updateById(employee);

        return new BaseResult("1", "成功", employee);
    }

    /**
     * 删除
     */
    @ApiOperation(value="删除", notes="")
    @SystemLogTag(description="删除", moduleName=moduleName)
    @RequestMapping(value="/delete", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult delete(@RequestBody Integer id) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setIsValid(0);
        employeeService.removeById(employee);
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
        List<Integer> employeeList = new ArrayList<>();
        for (Integer id : ids) {
            employeeList.add(id);
        }
        employeeService.removeByIds(employeeList);
        return new BaseResult();
    }

    /**
     * 查询详情
     */
    @ApiOperation(value="查询详情", notes="")
    @SystemLogTag(description="查询", moduleName=moduleName)
    @RequestMapping(value="/get", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<Employee> get(@RequestBody String id) {
        Employee employee = employeeService.getById(id);
        return new BaseResult("1", "成功", employee);
    }

    /**
     * 分页查询
     */
    @ApiOperation(value="查询", notes="")
    @SystemLogTag(description="查询列表", moduleName=moduleName)
    @RequestMapping(value="/getList", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult<BasePageResult<Employee>> getList(@RequestBody String paramJson) throws Exception {
        Map<String, ?> param = JsonObjUtils.json2map(paramJson);
        Page<Employee> page = new Page<Employee>(1, 10);
        if (param.get("page") != null) {
            page = JsonObjUtils.map2obj((Map<String, Object>) param.get("page"), Page.class);
        }
        QueryWrapper<Employee> wrapper = new QueryWrapper();
        Map<String, Object> queryParams;
        if (param.get("queryParams") != null) {
            queryParams = (Map<String, Object>) param.get("queryParams");

            if (null != queryParams.get("fullName") && !"".equals(queryParams.get("fullName").toString())) {
                String fullName = queryParams.get("fullName").toString();
                wrapper.and( q -> q.like("e.full_name", fullName).or().like("e.user_name", fullName));
            }
            if (null != queryParams.get("id") && !"".equals(queryParams.get("id").toString())) {
                String ids = departmentService.getChildrenIds(Integer.parseInt(queryParams.get("id").toString()));
                wrapper.apply("dp.id IN (" + ids.substring(1) + ")");
            }
            if (null != queryParams.get("departmentName") && !"".equals(queryParams.get("departmentName").toString())) {
                wrapper.like("dp.name", queryParams.get("departmentName"));
            }
        }

        page = employeeService.getList(page, wrapper);

        return new BaseResult(page);
    }

    /**
     * 获取短信验证码
     */
    @ApiOperation(value="获取短信验证码", notes="")
    @RequestMapping(value="/getSmsCode", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult getSmsCode(@RequestBody String paramJson) throws Exception {
        LoginGetSmsReq loginGetSmsReq = JsonObjUtils.json2obj(paramJson, LoginGetSmsReq.class);
        return employeeService.loginGetSmsCode(loginGetSmsReq);
    }

    /**
     * 忘记密码获取短信验证码
     */
    @ApiOperation(value="忘记密码获取短信验证码", notes="")
    @RequestMapping(value="/changePwdGetSmsCode", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult changePwdGetSmsCode(@RequestBody String paramJson) throws Exception {
        EmployeeGetSmsReq employeeGetSmsReq = JsonObjUtils.json2obj(paramJson, EmployeeGetSmsReq.class);
        return employeeService.changePwdGetSmsCode(employeeGetSmsReq);
    }

    /**
     * 修改密码
     */
    @ApiOperation(value="修改密码", notes="")
    @SystemLogTag(description="修改密码", moduleName=moduleName)
    @RequestMapping(value="/changePwd", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult changePwd(@RequestBody String param) throws Exception {
        EmployeeChangePwdReq employeeChangePwdReq = JsonObjUtils.json2obj(param, EmployeeChangePwdReq.class);
        return employeeService.changePwd(employeeChangePwdReq);
    }

    /**
     * 模板导入
     */
    @ApiOperation(value="模板导入", notes="")
    @SystemLogTag(description="模板导入", moduleName=moduleName)
    @RequestMapping(value="/excelImport", method={RequestMethod.POST})
    public BaseResult excelImport(MultipartFile file) throws Exception {
        return employeeService.excelImport(file);
    }

    /**
     * 模板导出
     */
    @ApiOperation(value="模板导出", notes="")
    @SystemLogTag(description="模板导出", moduleName=moduleName)
    @RequestMapping(value="/excelExport", method={RequestMethod.GET})
    public void excelExport(String ids) throws Exception {
        employeeService.excelExport(ids);
    }

    /**
     * 首登修改密码
     */
    @ApiOperation(value="首登修改密码", notes="")
    @SystemLogTag(description="修改密码", moduleName=moduleName)
    @RequestMapping(value="/firstModifyPwd", method={RequestMethod.POST})
    @SecretAnnotation(encode = true, decode = true)
    public BaseResult firstModifyPwd(@RequestBody String param) throws Exception {
        EmployeeFirstModifyPwdReq employeeFirstModifyPwdReq = JsonObjUtils.json2obj(param, EmployeeFirstModifyPwdReq.class);
        return employeeService.firstModifyPwd(employeeFirstModifyPwdReq);
    }

}
