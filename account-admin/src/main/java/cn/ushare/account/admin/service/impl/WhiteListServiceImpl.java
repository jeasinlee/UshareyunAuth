package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AuthMethodMapper;
import cn.ushare.account.admin.mapper.BlackListMapper;
import cn.ushare.account.admin.mapper.OnlinePolicyMapper;
import cn.ushare.account.admin.mapper.WhiteListMapper;
import cn.ushare.account.admin.service.SystemConfigService;
import cn.ushare.account.admin.service.WhiteListService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.*;
import cn.ushare.account.util.DateTimeUtil;
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

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author jixiang.li
 * @since 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class WhiteListServiceImpl extends ServiceImpl<WhiteListMapper, WhiteList> implements WhiteListService {

    @Autowired
    HttpServletResponse response;
    @Autowired
    WhiteListMapper whiteListMapper;
    @Autowired
    BlackListMapper blackListMapper;
    @Autowired
    SessionService sessionService;
    @Autowired
    SystemConfigService systemConfigService;
    @Autowired
    AuthMethodMapper authMethodMapper;
    @Autowired
    OnlinePolicyMapper onlinePolicyMapper;

    @Override
    public BaseResult add(WhiteList whiteList) {
        // 转换大写
        if (whiteList.getType() == 2) {// 1手机，2MAC，3IP
            String upValue = whiteList.getValue().toLowerCase();
            whiteList.setValue(upValue);
        }

        // 白名单是否重复
        QueryWrapper<WhiteList> whiteQuery = new QueryWrapper();
        whiteQuery.eq("type", whiteList.getType());
        whiteQuery.eq("value", whiteList.getValue());
        whiteQuery.eq("is_valid", 1);
        WhiteList whiteResult = whiteListMapper.selectOne(whiteQuery);
        if (whiteResult != null) {
            return new BaseResult("0", "已经加入白名单，请勿重复添加", null);
        }

        // 黑名单是否重复
        QueryWrapper<BlackList> blackQuery = new QueryWrapper();
        blackQuery.eq("type", whiteList.getType());
        blackQuery.eq("value", whiteList.getValue());
        blackQuery.eq("is_valid", 1);
        BlackList blackResult = blackListMapper.selectOne(blackQuery);
        if (blackResult != null) {
            return new BaseResult("0", "该值已存在于黑名单，请先在黑名单中删除", null);
        }

        AuthMethod authMethodEntity = authMethodMapper.selectById(Constant.AuthMethod.EMPLOYEE_AUTH);
        Integer policyId = authMethodEntity.getCustomPolicyId();
        OnlinePolicy userOnlinePolicy = onlinePolicyMapper.selectById(policyId);
        Integer onlinePeriod = 60;  //默认60分钟
        if (authMethodEntity.getUseCustomPolicy() == 1) {// 自定义上网策略
            onlinePeriod = userOnlinePolicy.getOnlinePeriod();
        } else {
            userOnlinePolicy = onlinePolicyMapper.selectById(100);
            if(userOnlinePolicy.getIsPeriodLimit()==1){
                onlinePeriod = userOnlinePolicy.getOnlinePeriod();
            }else {
                onlinePeriod = 99 * 365 * 24 * 60;
            }
        }

        // 新增
        whiteList.setIsValid(1);
        whiteList.setExpireTime(new DateTime().plusMinutes(onlinePeriod).toDate());
        whiteList.setUpdateTime(new Date());
        whiteListMapper.insert(whiteList);

        return new BaseResult();
    }

    @Override
    public BaseResult update(WhiteList whiteList) {
        // 转换大写
        if (whiteList.getType() == 2) {// 1手机，2MAC，3IP
            String upValue = whiteList.getValue().toLowerCase();
            whiteList.setValue(upValue);
        }

        // 白名单是否重复
        QueryWrapper<WhiteList> whiteQuery = new QueryWrapper();
        whiteQuery.eq("type", whiteList.getType());
        whiteQuery.eq("value", whiteList.getValue());
        whiteQuery.eq("is_valid", 1);
        WhiteList whiteResult = whiteListMapper.selectOne(whiteQuery);
        if (whiteResult != null && !whiteResult.getId().equals(whiteList.getId())) {
            return new BaseResult("0", "已经加入白名单，请勿重复添加", null);
        }

        // 黑名单是否重复
        QueryWrapper<BlackList> blackQuery = new QueryWrapper();
        blackQuery.eq("type", whiteList.getType());
        blackQuery.eq("value", whiteList.getValue());
        blackQuery.eq("is_valid", 1);
        BlackList blackResult = blackListMapper.selectOne(blackQuery);
        if (blackResult != null) {
            return new BaseResult("0", "该值已存在于黑名单，请先在黑名单中删除", null);
        }

        // 更新
        whiteListMapper.updateById(whiteList);

        return new BaseResult();
    }

    @Override
    public Page<WhiteList> getList(Page<WhiteList> page, QueryWrapper wrapper) {
        return page.setRecords(whiteListMapper.getList(page, wrapper));
    }

    @Override
    public BaseResult excelImportPhone(MultipartFile file) throws Exception {
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
                BaseResult checkResult = checkPhoneValue(row, i);
                if (checkResult.getReturnCode().equals("0")) {
                    return checkResult;
                }
            }

            // 文件内部不能有重复的phone
            List<String> phoneList = new ArrayList<>();
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                // 忽略空白行
                if (row == null) {
                    continue;
                }
                String phone = getCellValue(row.getCell(1));
                if (StringUtil.isBlank(phone)) {
                    return new BaseResult("0", "导入失败，第" + i + "行，手机号码不能为空", null);
                }
                if (phoneList.contains(phone)) {
                    return new BaseResult("0", "导入失败，第" + i + "行，excel文件中有重复的手机号码", null);
                }
                phoneList.add(phone);
            }

            // 存入数据库
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                // 忽略空白行
                if (row == null) {
                    continue;
                }
                BaseResult addResult = addPhoneValue(row, i);
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
    private BaseResult addPhoneValue(Row row, Integer rowNum) {
        String userName = getCellValue(row.getCell(0));
        String phone = getCellValue(row.getCell(1));
        String remark = getCellValue(row.getCell(2));
        WhiteList whiteList = new WhiteList();
        whiteList.setType(1);
        whiteList.setUserName(userName);
        whiteList.setValue(phone);
        whiteList.setRemark(remark);
        whiteList.setIsValid(1);
        whiteList.setUpdateTime(new Date());
        whiteListMapper.insert(whiteList);
        return new BaseResult();
    }

    /**
     * 输入值检查
     */
    private BaseResult checkPhoneValue(Row row, Integer rowNum) {
        String userName = getCellValue(row.getCell(0));
        String phone = getCellValue(row.getCell(1));
        String remark = getCellValue(row.getCell(2));

        // 空检查
        if (StringUtil.isBlank(phone)) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，手机不能为空", null);
        }

        // 格式检查
        Pattern pattern = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$");
        if (!pattern.matcher(phone).matches()) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，手机格式错误", null);
        }

        // 白名单是否重复
        QueryWrapper<WhiteList> whiteQuery = new QueryWrapper();
        whiteQuery.eq("type", 1);
        whiteQuery.eq("value", phone);
        whiteQuery.eq("is_valid", 1);
        WhiteList whiteResult = whiteListMapper.selectOne(whiteQuery);
        if (whiteResult != null) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，该手机已经加入白名单，请勿重复添加", null);
        }

        // 黑名单是否重复
        QueryWrapper<BlackList> blackQuery = new QueryWrapper();
        blackQuery.eq("type", 1);
        blackQuery.eq("value", phone);
        blackQuery.eq("is_valid", 1);
        BlackList blackResult = blackListMapper.selectOne(blackQuery);
        if (blackResult != null) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，该手机已存在于黑名单，请先在黑名单中删除", null);
        }

        return new BaseResult();
    }

    @Override
    public BaseResult excelImportMac(MultipartFile file) throws Exception {
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
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                // 忽略空白行
                if (row == null) {
                    continue;
                }
                BaseResult checkResult = checkMacValue(row, i);
                if (checkResult.getReturnCode().equals("0")) {
                    return checkResult;
                }
            }

            // 文件内部不能有重复的MAC
            List<String> macList = new ArrayList<>();
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                // 忽略空白行
                if (row == null) {
                    continue;
                }
                String mac = getCellValue(row.getCell(1));
                if (StringUtil.isBlank(mac)) {
                    return new BaseResult("0", "导入失败，第" + i + "行，MAC地址不能为空", null);
                }
                if (macList.contains(mac)) {
                    return new BaseResult("0", "导入失败，第" + i + "行，excel文件中有重复的MAC地址", null);
                }
                macList.add(mac);
            }

            // 存入数据库
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                // 忽略空白行
                if (row == null) {
                    continue;
                }

                BaseResult addResult = addMacValue(row, i);
                if (addResult.getReturnCode().equals("0")) {
                    throw new Exception(addResult.getReturnMsg());
                }
            }
        }

        return new BaseResult();
    }

    /**
     * 输入值检查
     */
    private BaseResult checkMacValue(Row row, Integer rowNum) {
        String userName = getCellValue(row.getCell(0));
        String mac = getCellValue(row.getCell(1));
        String remark = getCellValue(row.getCell(2));
        mac = mac.toLowerCase();

        // 空检查
        if (StringUtil.isBlank(mac)) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，MAC地址不能为空", null);
        }

        // 格式检查
        Pattern pattern = Pattern.compile("^([A-Fa-f0-9]{2}){6}$");
        if (!pattern.matcher(mac).matches()) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，MAC地址格式错误", null);
        }

        // 白名单是否重复
        QueryWrapper<WhiteList> whiteQuery = new QueryWrapper();
        whiteQuery.eq("type", 2);// 1手机，2MAC，3IP
        whiteQuery.eq("value", mac);
        whiteQuery.eq("is_valid", 1);
        WhiteList whiteResult = whiteListMapper.selectOne(whiteQuery);
        if (whiteResult != null) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，该MAC地址已经加入白名单，请勿重复添加", null);
        }

        // 黑名单是否重复
        QueryWrapper<BlackList> blackQuery = new QueryWrapper();
        blackQuery.eq("type", 2);
        blackQuery.eq("value", mac);
        blackQuery.eq("is_valid", 1);
        BlackList blackResult = blackListMapper.selectOne(blackQuery);
        if (blackResult != null) {
            return new BaseResult("0", "导入失败，第" + rowNum + "行，该MAC地址已存在于黑名单，请先在黑名单中删除", null);
        }

        return new BaseResult();
    }

    /**
     * 保存数据
     */
    private BaseResult addMacValue(Row row, Integer rowNum) {
        String userName = getCellValue(row.getCell(0));
        String mac = getCellValue(row.getCell(1));
        String remark = getCellValue(row.getCell(2));
        mac = mac.toLowerCase();

        WhiteList whiteList = new WhiteList();
        whiteList.setType(2);
        whiteList.setUserName(userName);
        whiteList.setValue(mac);
        whiteList.setRemark(remark);
        whiteList.setIsValid(1);
        whiteList.setUpdateTime(new Date());
        whiteListMapper.insert(whiteList);
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
    public void excelExportPhone(String ids) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            InputStream is = getClass().getClassLoader()
                    .getResourceAsStream("static/template/手机白名单模板.xlsx");
            Workbook workBook = new XSSFWorkbook(is);
            Sheet sheet = workBook.getSheetAt(0);

            String[] idArray = ids.split(",");
            for (int i = 0; i < idArray.length; i++) {
                Integer id = Integer.valueOf(idArray[i]);
                WhiteList whiteList = whiteListMapper.selectById(id);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(whiteList.getUserName());
                row.createCell(1).setCellValue(whiteList.getValue());
                row.createCell(2).setCellValue(whiteList.getRemark());
            }

            String fileName = "手机白名单" + DateTimeUtil.yyyymmddHHmmss
                    .format(new Date()) + ".xlsx";
            response.setHeader("Content-Disposition",
                    "attachment; filename="
                    + java.net.URLEncoder.encode(fileName, "UTF-8"));
            workBook.write(response.getOutputStream());
        } catch (Exception e) {
            log.error("Error Exception=", e);
        }
    }

    @Override
    public void excelExportMac(String ids) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            InputStream is = getClass().getClassLoader()
                    .getResourceAsStream("static/template/MAC白名单模板.xlsx");
            Workbook workBook = new XSSFWorkbook(is);
            Sheet sheet = workBook.getSheetAt(0);

            String[] idArray = ids.split(",");
            for (int i = 0; i < idArray.length; i++) {
                Integer id = Integer.valueOf(idArray[i]);
                WhiteList whiteList = whiteListMapper.selectById(id);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(whiteList.getUserName());
                row.createCell(1).setCellValue(whiteList.getValue());
                row.createCell(2).setCellValue(whiteList.getRemark());
            }

            String fileName = "MAC白名单" + DateTimeUtil.yyyymmddHHmmss
                    .format(new Date()) + ".xlsx";
            response.setHeader("Content-Disposition",
                    "attachment; filename="
                    + java.net.URLEncoder.encode(fileName, "UTF-8"));
            workBook.write(response.getOutputStream());
        } catch (Exception e) {
            log.error("Error Exception=", e);
        }
    }

}
