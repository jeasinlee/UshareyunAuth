package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.EmployeeMapper;
import cn.ushare.account.admin.service.*;
import cn.ushare.account.dto.*;
import cn.ushare.account.entity.*;
import cn.ushare.account.util.DateTimeUtil;
import cn.ushare.account.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author jixiang.li
 * @date 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    HttpServletRequest request;
    @Autowired
    HttpServletResponse response;
    @Autowired
    EmployeeMapper employeeMapper;
    @Autowired
    DepartmentService departmentService;
    @Autowired
    BandwidthService bandwidthService;
    @Autowired
    SmsSendService smsSendService;
    @Autowired
    SystemConfigService systemConfigService;

    @Autowired
    @Qualifier("adService")
    AdService adService;
    @Autowired
    SsidService ssidService;

    @Override
    public Page<Employee> getList(Page<Employee> page, QueryWrapper wrapper) {
        return page.setRecords(employeeMapper.getList(page, wrapper));
    }

    @Override
    public Integer getDepartmentBandwidth(String userName) {
        QueryWrapper<Employee> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", userName);
        queryWrapper.eq("is_valid", 1);
        Employee employee = employeeMapper.selectOne(queryWrapper);

        if(null!=employee) {
            Department depart = departmentService.getById(employee.getDepartmentId());
            if (depart != null) {
                return depart.getBandwidthId();
            } else {
                return null;
            }
        }
        return null;
    }

    @Override
    public BaseResult excelImport(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        if (!fileName.matches("^.+\\.(?i)(xls)$") && !fileName.matches("^.+\\.(?i)(xlsx)$")) {
            log.error("上传文件格式不正确");
        }
        Workbook workbook = null;
        try {
            InputStream is = file.getInputStream();
            if (fileName.endsWith("xlsx")) {// excel2007
                workbook = new XSSFWorkbook(is);
            }
            if (fileName.endsWith("xls")) {// excel2003
                workbook = new HSSFWorkbook(is);
            }
        } catch (Exception e) {
            log.error("Parse excel Error Exception=", e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    log.error(String.format("parse excel exception!"), e);
                }
            }
        }
        if (workbook != null) {
            Sheet sheet = workbook.getSheetAt(0);
            // 输入值检查
            // getLastRowNum()值是n-1，总共1行则返回0，i值从第2行算起，第1行为标题
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                // 忽略空白行
                if (row == null) {
                    continue;
                }
                BaseResult checkResult = checkValue(row, i);
                if (checkResult.getReturnCode().equals("0")) {
                    return checkResult;
                }
            }

            // 文件内部不能有重复的账号
            List<String> userNameList = new ArrayList<>();
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                // 忽略空白行
                if (row == null) {
                    continue;
                }
                String userName = getCellValue(row.getCell(1));
                if (StringUtil.isBlank(userName)) {
                    return new BaseResult("0", "导入失败，第" + i + "行，账号不能为空", null);
                }
                if (userNameList.contains(userName)) {
                    return new BaseResult("0", "导入失败，第" + i + "行，excel文件中有重复的账号", null);
                }
                userNameList.add(userName);
            }

            // 存入数据库
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                // 忽略空白行
                if (row == null) {
                    continue;
                }
                BaseResult addResult = addValue(row, i);
                if (addResult.getReturnCode().equals("0")) {
                    throw new Exception(addResult.getReturnMsg());
                }
            }
        }

        return new BaseResult();
    }

    /**
     * 保存数据
     */
    private BaseResult addValue(Row row, Integer rowNum) {
        String fullName = getCellValue(row.getCell(0));
        String userName = getCellValue(row.getCell(1));
        String password = getCellValue(row.getCell(2));
        String departmentName = getCellValue(row.getCell(3));
        String bandwidthName = getCellValue(row.getCell(4));
        String terminalNumStr = getCellValue(row.getCell(5));
        String sex = getCellValue(row.getCell(6));
        String employeeAuth = getCellValue(row.getCell(7));
        String isUsing = getCellValue(row.getCell(8));
        String phone = getCellValue(row.getCell(9));
        String isBindMac = getCellValue(row.getCell(10));
        String bindMacs = getCellValue(row.getCell(11));

        // 查询部门ID
        QueryWrapper<Department> departQuery = new QueryWrapper();
        departQuery.eq("name", departmentName);
        departQuery.eq("is_valid", 1);
        Department department = departmentService.getOne(departQuery);

        // 查询带宽ID
        QueryWrapper<Bandwidth> bandQuery = new QueryWrapper();
        bandQuery.eq("name", bandwidthName);
        bandQuery.eq("is_valid", 1);
        Bandwidth bandwidth = bandwidthService.getOne(bandQuery);

        Integer terminalNum = null;
        if (StringUtil.isNotBlank(terminalNumStr)) {
            terminalNum = Integer.valueOf(terminalNumStr);
        }

        Employee employee = new Employee();
        employee.setFullName(fullName);
        employee.setUserName(userName);
        employee.setPassword(password);
        employee.setDepartmentId(department.getId());
        employee.setBandwidthId(bandwidth.getId());
        employee.setTerminalNum(terminalNum);
        employee.setIsBindMac(Integer.parseInt(isBindMac));
        employee.setBindMacs(bindMacs.toLowerCase());
        employee.setPhone(phone);
        employee.setSex(sex.equals("男") ? 1 : 0);
        employee.setIsEmployeeAuthEnable(employeeAuth.equals("是") ? 1 : 0);
        employee.setIsUsing(isUsing.equals("是") ? 1 : 0);
        employeeMapper.insert(employee);

        return new BaseResult();
    }

    /**
     * 输入值检查
     */
    private BaseResult checkValue(Row row, Integer rowNum) {
        String fullName = getCellValue(row.getCell(0));
        String userName = getCellValue(row.getCell(1));
        String password = getCellValue(row.getCell(2));
        String departmentName = getCellValue(row.getCell(3));
        String bandwidthName = getCellValue(row.getCell(4));
        String terminalNum = getCellValue(row.getCell(5));
        String sex = getCellValue(row.getCell(6));
        String employeeAuth = getCellValue(row.getCell(7));
        String isUsing = getCellValue(row.getCell(8));
        String phone = getCellValue(row.getCell(9));

        // 空检查
        if (StringUtil.isBlank(fullName)) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行姓名不能为空", null);
        }
        if (StringUtil.isBlank(userName)) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，账号不能为空", null);
        }
        if (StringUtil.isBlank(password)) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，密码不能为空", null);
        }
        if (StringUtil.isBlank(departmentName)) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，部门不能为空", null);
        }
        if (StringUtil.isBlank(bandwidthName)) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，带宽不能为空", null);
        }
        if (StringUtil.isBlank(sex)) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，性别不能为空", null);
        }
        if (StringUtil.isBlank(employeeAuth)) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，员工授权不能为空", null);
        }
        if (StringUtil.isBlank(isUsing)) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，是否有效不能为空", null);
        }

        // 账号、密码格式检查
        Pattern pattern = Pattern.compile("^[A-Za-z0-9_-]{2,20}$");
        if (!pattern.matcher(userName).matches()) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，账号格式错误，(数字/字母/下划线/横杆)(4-16位)", null);
        }
        // Pattern pwdPattern =
        // Pattern.compile("^[A-Za-z0-9_-]{4,16}$^(?:\\d+|[a-zA-Z]+|[!@#$%^&*]+)$");
        // if (!pwdPattern.matcher(password).matches()) {
        // return new BaseResult("0", "导入失败，第" + rowNum +
        // "行，密码格式错误，必须是字母数字字符组成(4-16位)", null);
        // }

        // 终端数格式检查
        if (StringUtil.isNotBlank(terminalNum)) {
            Pattern terPattern = Pattern.compile("^[1-9]*$");
            if (!terPattern.matcher(terminalNum).matches()) {
                return new BaseResult("0", "导入失败，第" + rowNum + "行，终端数格式错误，必须是数字", null);
            }
        }

        // 账号重复检查
        QueryWrapper<Employee> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", userName);
        queryWrapper.eq("is_valid", 1);
        Employee employee = employeeMapper.selectOne(queryWrapper);
        if (employee != null) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，账号重复", null);
        }

        // 部门名称检查
        QueryWrapper<Department> departQuery = new QueryWrapper();
        departQuery.eq("name", departmentName);
        departQuery.eq("is_valid", 1);
        Department department = departmentService.getOne(departQuery);
        if (department == null) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，部门名称错误", null);
        }

        // 带宽检查
        QueryWrapper<Bandwidth> bandQuery = new QueryWrapper();
        bandQuery.eq("name", bandwidthName);
        bandQuery.eq("is_valid", 1);
        Bandwidth bandwidth = bandwidthService.getOne(bandQuery);
        if (bandwidth == null) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，没有该带宽选项", null);
        }

        return new BaseResult();
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return HSSFDateUtil.getJavaDate(cell.getNumericCellValue()).toString();
            } else {
                return new BigDecimal(cell.getNumericCellValue()).toString();
            }
        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
            return StringUtils.trimToEmpty(cell.getStringCellValue());
        } else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            return StringUtils.trimToEmpty(cell.getCellFormula());
        } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return "";
        } else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (cell.getCellType() == Cell.CELL_TYPE_ERROR) {
            return "ERROR";
        } else {
            return cell.toString().trim();
        }
    }

    @Override
    public void excelExport(String ids) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            InputStream is = getClass().getClassLoader()
                    .getResourceAsStream("static/template/员工模板.xlsx");
            Workbook workBook = new XSSFWorkbook(is);
            Sheet sheet = workBook.getSheetAt(0);

            String[] idArray = ids.split(",");
            for (int i = 0; i < idArray.length; i++) {
                Integer id = Integer.valueOf(idArray[i]);
                Employee employee = employeeMapper.selectById(id);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(employee.getFullName());
                row.createCell(1).setCellValue(employee.getUserName());
                row.createCell(2).setCellValue(employee.getPassword());
                Department department = departmentService.getById(employee.getDepartmentId());
                row.createCell(3).setCellValue(department.getName());
                Bandwidth bandwidth = bandwidthService.getById(employee.getBandwidthId());
                row.createCell(4).setCellValue(bandwidth.getName());
                if (employee.getTerminalNum() != null) {
                    row.createCell(5).setCellValue(employee.getTerminalNum() + "");
                }
                row.createCell(6).setCellValue(
                        employee.getSex() == null || employee.getSex() == 0 ? "女" : "男");
                row.createCell(7).setCellValue(
                        employee.getIsEmployeeAuthEnable() == null || employee.getIsEmployeeAuthEnable() == 0 ? "否" : "是");
                row.createCell(8).setCellValue(
                        employee.getIsUsing() == null || employee.getIsUsing() == 0 ? "否" : "是");
                row.createCell(9).setCellValue(employee.getPhone());
                row.createCell(10).setCellValue(employee.getIsBindMac());
                row.createCell(11).setCellValue(employee.getBindMacs());
            }

            String fileName = "员工" + DateTimeUtil.yyyymmddHHmmss
                    .format(new Date()) + ".xlsx";
            response.setHeader("Content-Disposition",
                    "attachment; filename="
                    + java.net.URLEncoder.encode(fileName, "UTF-8"));
            workBook.write(response.getOutputStream());

//            FileOutputStream out = new FileOutputStream("d://uploadFile//test.xlsx");
//            workBook.write(out);
//            out.flush();
//            out.close();
//            is.close();
        } catch (Exception e) {
            log.error("Error Exception=", e);
        }
    }

    @Override
    public BaseResult loginGetSmsCode(LoginGetSmsReq param) throws Exception {
        String checkCode = param.getCheckCode();

        String showCode = systemConfigService.getByCode("VALID_CODE");
        if("1".equals(showCode)) {
            // 检查图片验证码
            if (request.getSession().getAttribute("checkCode") == null) {
                return new BaseResult("0", "验证码失效", null);
            }
            String rightCheckCode = (String) request.getSession()
                    .getAttribute("checkCode");
            if (StringUtils.isBlank(checkCode)
                    || StringUtils.isBlank(rightCheckCode)
                    || !checkCode.equals(rightCheckCode)) {
                request.getSession().removeAttribute("checkCode");
                return new BaseResult("0", "图片验证码错误", null);
            }

        }
        request.getSession().removeAttribute("checkCode");

        QueryWrapper<Ssid> wrapper= new QueryWrapper<>();
        wrapper.eq("name", param.getSsid());
        wrapper.eq("is_valid", 1);
        Ssid ssidModel = ssidService.getOne(wrapper, false);
        // AD域是否开启
        String adStatus = systemConfigService.getByCode("AD-DOMAIN-STATUS");
        if("1".equals(adStatus)){
            if(null!=ssidModel && 1==ssidModel.getIsEmployee()){
                //验证手机号是否在LDAP域
                List<LdapUser> user = adService.findUser(param.getPhone());
                boolean phoneExsit = CollectionUtils.isNotEmpty(user);
                if(!phoneExsit){
                    return new BaseResult("0", "手机号不合法", null);
                }
            }
        }

        String smsCode = (int) ((Math.random()*9 + 1) * 1000) + "";
        BaseResult result = smsSendService.send(param.getPhone(), smsCode, 1);
        if (result.getReturnCode().equals("1")) {
            // 缓存参数
            request.getSession().setAttribute("phone", param.getPhone());
            request.getSession().setAttribute("smsCode", smsCode);
        }

        return result;
    }

    @Override
    public BaseResult changePwdGetSmsCode(EmployeeGetSmsReq param) throws Exception {
        String checkCode = param.getCheckCode();
        String showCode = systemConfigService.getByCode("VALID_CODE");
        if("1".equals(showCode)) {
            // 检查图片验证码
            if (request.getSession().getAttribute("checkCode") == null) {
                return new BaseResult("0", "验证码失效", null);
            }
            String rightCheckCode = (String) request.getSession()
                    .getAttribute("checkCode");
            if (StringUtils.isBlank(checkCode)
                    || StringUtils.isBlank(rightCheckCode)
                    || !checkCode.equals(rightCheckCode)) {
                request.getSession().removeAttribute("checkCode");
                return new BaseResult("0", "图片验证码错误", null);
            }

        }
        request.getSession().removeAttribute("checkCode");

        // 根据账号查询用户手机
        QueryWrapper<Employee> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", param.getUserName());
        queryWrapper.eq("is_valid", 1);
        Employee employee = employeeMapper.selectOne(queryWrapper);
        if (employee == null) {
            return new BaseResult("0", "用户名错误", null);
        }

        if (employee.getPhone() == null) {
            return new BaseResult("0", "该账号未登记手机号码", null);
        }

        String smsCode = (int) ((Math.random()*9 + 1) * 1000) + "";
        BaseResult result = smsSendService.send(employee.getPhone(), smsCode, 2);
        if (result.getReturnCode().equals("1")) {
            // 缓存参数
            request.getSession().setAttribute("userName", param.getUserName());
            request.getSession().setAttribute("smsCode", smsCode);
        }

        return result;
    }

    @Override
    public BaseResult changePwd(EmployeeChangePwdReq param) throws Exception {
        String smsCode = param.getSmsCode();

        // 检查短信验证码
        String rightSmsCode = (String) request.getSession()
                .getAttribute("smsCode");
        if (StringUtils.isBlank(smsCode)
                || StringUtils.isBlank(rightSmsCode)
                || !smsCode.equals(rightSmsCode)) {
            request.getSession().removeAttribute("smsCode");
            return new BaseResult("0", "短信验证码错误", null);
        }
        request.getSession().removeAttribute("checkCode");

        // 检查参数
        if (StringUtils.isBlank(param.getNewPassword())) {
            return new BaseResult("0", "密码不能为空", null);
        }
        if (request.getSession().getAttribute("userName") == null) {
            return new BaseResult("0", "用户名不能为空", null);
        }
        String userName = (String) request.getSession()
                .getAttribute("userName");
        if (StringUtils.isBlank(userName)) {
            return new BaseResult("0", "用户名不能为空", null);
        }

        // 查询用户
        QueryWrapper<Employee> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", userName);
        queryWrapper.eq("is_valid", 1);
        Employee employee = employeeMapper.selectOne(queryWrapper);
        if (employee == null) {
            return new BaseResult("0", "用户名错误", null);
        }

        // 更新密码
        employee.setPassword(param.getNewPassword());
        employeeMapper.updateById(employee);
        return new BaseResult();
    }

    @Override
    public BaseResult firstModifyPwd(EmployeeFirstModifyPwdReq param) throws Exception {
        // 查询用户
        QueryWrapper<Employee> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_name", param.getUserName());
        queryWrapper.eq("password", param.getOldPwd());
        queryWrapper.eq("is_valid", 1);
        Employee employee = employeeMapper.selectOne(queryWrapper);
        if (employee == null) {
            return new BaseResult("0", "用户名或原密码错误", null);
        }

        employee.setPassword(param.getNewPassword());
        employee.setIsFinish(1);
        employeeMapper.updateById(employee);
        return new BaseResult();
    }

    @Override
    public BaseResult setBandwidthNull(Integer id) {
        employeeMapper.setBandwidthNull(id);
        return new BaseResult();
    }

}
