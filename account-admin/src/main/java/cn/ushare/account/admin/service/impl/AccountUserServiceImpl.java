package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AccountUserMapper;
import cn.ushare.account.admin.service.*;
import cn.ushare.account.dto.AccountUserForgetReq;
import cn.ushare.account.entity.*;
import cn.ushare.account.util.DateTimeUtil;
import cn.ushare.account.util.EncryptUtils;
import cn.ushare.account.util.JsonObjUtils;
import cn.ushare.account.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
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
public class AccountUserServiceImpl extends ServiceImpl<AccountUserMapper, AccountUser> implements AccountUserService {

    @Autowired
    HttpServletRequest request;
    @Autowired
    HttpServletResponse response;
    @Autowired
    AccountUserMapper accountUserMapper;
    @Autowired
    SmsRecordService smsRecordService;
    @Autowired
    SystemConfigService systemConfigService;
    @Autowired
    SmsConfigService smsConfigService;
    @Autowired
    AccountChargePolicyService chargePolicyService;
    @Autowired
    AccountUserGroupService userGroupService;

    @Override
    public Page<AccountUser> getList(Page<AccountUser> page, QueryWrapper wrapper) {
        List<AccountUser> accountUsers = accountUserMapper.getList(page, wrapper);
        return page.setRecords(accountUsers);
    }

    @Override
    public Page<AccountUser> getLockedList(Page<AccountUser> page, QueryWrapper wrapper) {
        List<AccountUser> accountUsers = accountUserMapper.getLockedList(page, wrapper);
        return page.setRecords(accountUsers);
    }

    @Override
    public Page<AccountUser> getDebtList(Page<AccountUser> page, QueryWrapper wrapper) {
        List<AccountUser> accountUsers = accountUserMapper.getDebtList(page, wrapper);
        return page.setRecords(accountUsers);
    }

    @Override
    public BaseResult addOrUpdate(AccountUser accountUser) {
        //注册会传验证码，修改个人信息则不会
        if(StringUtils.isNotEmpty(accountUser.getSmsCode())) {
            //验证短信验证码
            QueryWrapper<SmsRecord> queryWrapper = new QueryWrapper();
            queryWrapper.eq("phone", accountUser.getMobile());
            queryWrapper.eq("result", 1);
            queryWrapper.eq("business_type", 3);
            queryWrapper.eq("is_valid", 1);
            queryWrapper.orderByDesc("create_time");
            List<SmsRecord> list = smsRecordService.list(queryWrapper);
            if (list.size() == 0) {
                return new BaseResult("0", "没有短信记录", null);
            }
            SmsRecord smsRecord = list.get(0);
            if (!smsRecord.getCheckCode().equals(accountUser.getSmsCode())) {
                return new BaseResult("0", "短信验证码错误", null);
            }

            int expireMin = 15;
            // 短信过期时间
            String smsServerId = systemConfigService.getByCode("SMS-SERVER-ID");
            SmsConfig smsConfig = smsConfigService.getById(smsServerId);
            if (null != smsConfig) {
                expireMin = smsConfig.getExpireTime();
            }

            // 短信是否过期
            Long createTime = DateTimeUtil.getMillis(smsRecord.getCreateTime());
            if (System.currentTimeMillis() - createTime > expireMin * 60 * 1000) {
                return new BaseResult("0", "短信验证码过期", null);
            }
        }

        if(!StringUtil.validPwdForXiaoxiang(accountUser.getPwd())){
            return new BaseResult("0", "密码必须是8到16位数字或字母", null);
        }

        accountUser.setPwd(EncryptUtils.encodeBase64String(accountUser.getPwd()));
        QueryWrapper<AccountUser> query = new QueryWrapper();
        query.eq("login_name", accountUser.getLoginName());
        AccountUser repeatAccountUser = accountUserMapper.selectOne(query);
        if (repeatAccountUser == null) {
            Date date = new Date();
            accountUser.setCreateTime(date);
            accountUser.setUpdateTime(date);

            AccountChargePolicy chargePolicy = chargePolicyService.getById(accountUser.getChargePolicyId());
            if(null == accountUser.getBindMacNum()){
                accountUser.setBindMacNum(chargePolicy.getBindMacNum());
            }
            if(null == accountUser.getExpireTime()){
                Date expireT;
                switch (chargePolicy.getUnit()){
                    case 0:
                        //天
                        expireT = new DateTime().plusDays(chargePolicy.getTotalNum()).toDate();
                        break;
                    case 1:
                        //月
                        expireT = new DateTime().plusMonths(chargePolicy.getTotalNum()).toDate();
                        break;
                    case 2:
                        //年
                        expireT = new DateTime().plusYears(chargePolicy.getTotalNum()).toDate();
                        break;
                    case 3:
                        //小时
                        expireT = new DateTime().plusHours(chargePolicy.getTotalNum()).toDate();
                        break;
                    default:
                        //月
                        expireT = new DateTime().plusDays(chargePolicy.getTotalNum()).toDate();
                }
                accountUser.setExpireTime(expireT);
            }

            accountUserMapper.insert(accountUser);
            return new BaseResult();
        }
        accountUser.setId(repeatAccountUser.getId());
        accountUserMapper.updateById(accountUser);
        return new BaseResult();
    }

    @Override
    public BaseResult login(String accountUserJson) throws Exception {
        AccountUser accountUser = JsonObjUtils.json2obj(accountUserJson, AccountUser.class);
        // 检查密码
        AccountUser user = accountUserMapper.getDetail(accountUser.getLoginName(), 1);
        if (user == null) {
            return new BaseResult("0", "账号不存在", null);
        }
        if (!EncryptUtils.decodeBase64String(user.getPwd()).equals(accountUser.getPwd())) {
            return new BaseResult("0", "密码错误", null);
        }
        request.getSession().setAttribute("userInfo", user);

        return new BaseResult(user);
    }

    @Override
    public AccountUser getDetail(String loginName, int isValid) {
        return accountUserMapper.getDetail(loginName, isValid);
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
                } else if (addResult.getReturnCode().equals("10")) {
                    log.info("重复账号跳过=====" + addResult.getData());
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
        String email = getCellValue(row.getCell(3));
        String idcard = getCellValue(row.getCell(4));
        String address = getCellValue(row.getCell(5));
        String expireTimeStr = getCellValue(row.getCell(6));
        String groupName = getCellValue(row.getCell(7));
        String policyName = getCellValue(row.getCell(8));
        String terminalNumStr = getCellValue(row.getCell(9));
        String mobile = getCellValue(row.getCell(10));

        Date expireTime = DateTimeUtil.parseDate(expireTimeStr, "yyyy-MM-dd");

        // 查询分组ID
        QueryWrapper<AccountUserGroup> groupQuery = new QueryWrapper();
        groupQuery.eq("group_name", groupName);
        AccountUserGroup accountUserGroup = userGroupService.getOne(groupQuery, false);

        // 查询策略ID
        QueryWrapper<AccountChargePolicy> policyQuery = new QueryWrapper();
        policyQuery.eq("policy_name", policyName);
        policyQuery.eq("is_valid", 1);
        AccountChargePolicy chargePolicy = chargePolicyService.getOne(policyQuery);

        Integer terminalNum = 0;
        if (StringUtil.isNotBlank(terminalNumStr)) {
            terminalNum = Integer.valueOf(terminalNumStr);
        }

        // 账号重复检查
        QueryWrapper<AccountUser> queryWrapper = new QueryWrapper();
        queryWrapper.eq("login_name", userName);
        AccountUser user = accountUserMapper.selectOne(queryWrapper);
        if (user != null) {
            return new BaseResult("10", "导入失败，第" + rowNum + "行，账号重复", userName);
        }

        AccountUser accountUser = new AccountUser();
        accountUser.setNickName(fullName);
        accountUser.setLoginName(userName);
        if(StringUtils.isNotEmpty(password)) {
            accountUser.setPwd(EncryptUtils.encodeBase64String(password));
        } else {
            accountUser.setPwd(EncryptUtils.encodeBase64String(userName.substring(12)));
        }
        accountUser.setEmail(email);
        accountUser.setIdcard(idcard);
        accountUser.setAddress(address);
        accountUser.setBindMacNum(terminalNum);
        accountUser.setExpireTime(expireTime);
        accountUser.setAccountGroupId(accountUserGroup.getId());
        accountUser.setAccountGroupName(accountUserGroup.getGroupName());
        accountUser.setChargePolicyId(chargePolicy.getId());
        accountUser.setMobile(mobile);

        accountUserMapper.insert(accountUser);

        return new BaseResult();
    }

    /**
     * 输入值检查
     */
    private BaseResult checkValue(Row row, Integer rowNum) {
        String fullName = getCellValue(row.getCell(0));
        String userName = getCellValue(row.getCell(1));
        String password = getCellValue(row.getCell(2));
        String email = getCellValue(row.getCell(3));
        String idcard = getCellValue(row.getCell(4));
        String address = getCellValue(row.getCell(5));
        String expireTimeStr = getCellValue(row.getCell(6));
        String groupName = getCellValue(row.getCell(7));
        String policyName = getCellValue(row.getCell(8));
        String terminalNumStr = getCellValue(row.getCell(9));
        String mobile = getCellValue(row.getCell(10));

        // 空检查
//        if (StringUtil.isBlank(fullName)) {
//            return new BaseResult("0", "导入失败，第" + rowNum + "行姓名不能为空", null);
//        }
//        if (StringUtil.isBlank(userName)) {
//            return new BaseResult("0", "导入失败，第" + rowNum + "行，账号不能为空", null);
//        }
//        if (StringUtil.isBlank(password)) {
//            return new BaseResult("0", "导入失败，第" + rowNum + "行，密码不能为空", null);
//        }
//
//        if (StringUtil.isBlank(idcard)) {
//            return new BaseResult("0", "导入失败，第" + rowNum + "行，身份证号不能为空", null);
//        }
//        if (StringUtil.isBlank(expireTimeStr)) {
//            return new BaseResult("0", "导入失败，第" + rowNum + "行，过期时间不能为空", null);
//        }
//        if (StringUtil.isBlank(groupId)) {
//            return new BaseResult("0", "导入失败，第" + rowNum + "行，分组ID不能为空", null);
//        }
//        if (StringUtil.isBlank(policyId)) {
//            return new BaseResult("0", "导入失败，第" + rowNum + "行，策略ID不能为空", null);
//        }

        // 账号、密码格式检查
//        Pattern pattern = Pattern.compile("^[A-Za-z0-9_-]{2,20}$");
//        if (!pattern.matcher(userName).matches()) {
//            return new BaseResult("0", "导入失败，第" + rowNum + "行，账号格式错误，(数字/字母/下划线/横杆)(4-16位)", null);
//        }
        // Pattern pwdPattern =
        // Pattern.compile("^[A-Za-z0-9_-]{4,16}$^(?:\\d+|[a-zA-Z]+|[!@#$%^&*]+)$");
        // if (!pwdPattern.matcher(password).matches()) {
        // return new BaseResult("0", "导入失败，第" + rowNum +
        // "行，密码格式错误，必须是字母数字字符组成(4-16位)", null);
        // }

        // 终端数格式检查
        if (StringUtil.isNotBlank(terminalNumStr)) {
            Pattern terPattern = Pattern.compile("^[1-9]*$");
            if (!terPattern.matcher(terminalNumStr).matches()) {
                return new BaseResult("0", "导入失败，第" + rowNum + "行，终端数格式错误，必须是数字", null);
            }
        }

        // 账号重复检查
        QueryWrapper<AccountUser> queryWrapper = new QueryWrapper();
        queryWrapper.eq("login_name", userName);
        AccountUser user = accountUserMapper.selectOne(queryWrapper);
        if (user != null) {
            return new BaseResult("10", "导入失败，第" + rowNum + "行，账号重复", null);
        }

        // 部门名称检查
        QueryWrapper<AccountUserGroup> departQuery = new QueryWrapper();
        departQuery.eq("group_name", groupName);
        AccountUserGroup userGroup = userGroupService.getOne(departQuery);
        if (userGroup == null) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，分组名称错误", null);
        }

        // 带宽检查
        QueryWrapper<AccountChargePolicy> bandQuery = new QueryWrapper();
        bandQuery.eq("policy_name", policyName);
        bandQuery.eq("is_valid", 1);
        AccountChargePolicy chargePolicy = chargePolicyService.getOne(bandQuery);
        if (chargePolicy == null) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，没有该策略选项", null);
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
    public void excelExport(String ids) {
        try {
            InputStream is = getClass().getClassLoader()
                    .getResourceAsStream("static/template/账户模板.xlsx");
            Workbook workBook = new XSSFWorkbook(is);
            Sheet sheet = workBook.getSheetAt(0);

            String[] idArray = ids.split(",");
            for (int i = 0; i < idArray.length; i++) {
                Integer id = Integer.valueOf(idArray[i]);
                AccountUser accountUser = accountUserMapper.selectById(id);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(accountUser.getNickName());
                row.createCell(1).setCellValue(accountUser.getLoginName());
                row.createCell(2).setCellValue(accountUser.getPwd());
                if(null != accountUser.getEmail()) {
                    row.createCell(3).setCellValue(accountUser.getEmail());
                }
                row.createCell(4).setCellValue(accountUser.getIdcard());
                row.createCell(5).setCellValue(accountUser.getAddress());
                row.createCell(6).setCellValue(DateTimeUtil.date2Str(accountUser.getExpireTime(),
                        new SimpleDateFormat("yyyy-MM-dd")));
                row.createCell(7).setCellValue(accountUser.getAccountGroupId());
                row.createCell(8).setCellValue(accountUser.getChargePolicyId());
                row.createCell(9).setCellValue(accountUser.getBindMacNum());
                row.createCell(10).setCellValue(accountUser.getMobile());
            }

            String fileName = "账户列表-" + DateTimeUtil.yyyymmddHHmmss
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
    public void updateByIds(List<Integer> ids) {
        accountUserMapper.updateByIds(ids);
    }

    @Override
    public BaseResult forgotPwd(AccountUserForgetReq forgetReq, AccountUser accountUser) {
//        if(StringUtils.isNotEmpty(accountUser.getMobile()) && forgetReq.getMobile().equals(accountUser.getMobile())){
//            return new BaseResult("0", "绑定手机号不符合，不能修改密码", null);
//        }

        //验证短信验证码
        QueryWrapper<SmsRecord> queryWrapper = new QueryWrapper();
        queryWrapper.eq("phone", accountUser.getMobile());
        queryWrapper.eq("result", 1);
        queryWrapper.eq("business_type", 3);
        queryWrapper.eq("is_valid", 1);
        queryWrapper.orderByDesc("create_time");
        List<SmsRecord> list = smsRecordService.list(queryWrapper);
        if (list.size() == 0) {
            return new BaseResult("0", "没有短信记录", null);
        }
        SmsRecord smsRecord = list.get(0);
        if (!smsRecord.getCheckCode().equals(forgetReq.getCode())) {
            return new BaseResult("0", "短信验证码错误", null);
        }

        int expireMin = 15;
        // 短信过期时间
        String smsServerId = systemConfigService.getByCode("SMS-SERVER-ID");
        SmsConfig smsConfig = smsConfigService.getById(smsServerId);
        if (null != smsConfig) {
            expireMin = smsConfig.getExpireTime();
        }

        // 短信是否过期
        Long createTime = DateTimeUtil.getMillis(smsRecord.getCreateTime());
        if (System.currentTimeMillis() - createTime > expireMin * 60 * 1000) {
            return new BaseResult("0", "短信验证码过期", null);
        }

        if(StringUtils.isNotEmpty(forgetReq.getReplacePwd()) &&
                !StringUtil.validPwdForXiaoxiang(forgetReq.getReplacePwd())){
            return new BaseResult("0", "密码必须是8到16位数字或字母", null);
        }
        accountUser.setPwd(EncryptUtils.encodeBase64String(forgetReq.getReplacePwd()));
        accountUserMapper.updateById(accountUser);

        return new BaseResult();
    }

}
