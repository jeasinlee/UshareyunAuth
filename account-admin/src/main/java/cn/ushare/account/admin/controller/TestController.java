package cn.ushare.account.admin.controller;

import cn.ushare.account.admin.service.*;
import cn.ushare.account.dto.LdapUser;
import cn.ushare.account.dto.UserLoginReq;
import cn.ushare.account.entity.*;
import cn.ushare.account.util.RSAEncryption;
import cn.ushare.account.util.RsaUtil;
import cn.ushare.account.util.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 测试接口，模拟外部服务器返回
 */
@Api(tags = "TestController", description = "Test")
@RestController
@Slf4j
@RequestMapping("/test")
public class TestController {

    @Autowired
    AuthRecordService authRecordService;
    @Autowired
    AuthUserService authUserService;
    @Autowired
    EmployeeService employeeService;
    @Autowired
    AuthParamService authParamService;
    @Autowired
    @Qualifier("adService")
    AdService adService;
    
    /**
     * 系统时间查询
     */
    @ApiOperation(value = "系统时间查询", notes = "")
    @RequestMapping(value = "/systemTime", method = { RequestMethod.POST })
    public BaseResult<String> systemTime() throws Exception {
        return new BaseResult("2019-10-10 01:02:03");
    }

    @ApiOperation(value = "获取有效ad", notes = "")
    @RequestMapping(value = "/users", method = { RequestMethod.GET })
    public BaseResult<String> users() throws Exception {
        List<String> allUids = adService.getAllUids();
        log.info("ad users===="+ allUids);
        return new BaseResult(allUids);
    }

    @ApiOperation(value = "获取有效ad", notes = "")
    @RequestMapping(value = "/getUser", method = { RequestMethod.GET })
    public BaseResult<String> findUser(@RequestParam String mobile) throws Exception {
        List<LdapUser> alls = adService.findUser(mobile);
        log.info("ad getUser===="+ alls);
        return new BaseResult(alls.get(0));
    }

    @ApiOperation(value = "获取有效ad", notes = "")
    @RequestMapping(value = "/findUserByName", method = { RequestMethod.GET })
    public BaseResult<String> findUserByName(@RequestParam String userName) throws Exception {
        List<LdapUser> alls = adService.findUserByName(userName);
        log.info("ad findUserByName===="+ alls);
        return new BaseResult(alls);
    }

    /**
     * 账户查询
     */
    @ApiOperation(value = "账户查询", notes = "")
    @RequestMapping(value = "/accountCheck", method = { RequestMethod.POST })
    public String accountCheck(@RequestBody UserLoginReq param) throws Exception {
        if (param.getUserName().equals("admin") && param.getPassword().equals("admin")) {
            return "{\"code\": \"1\", \"msg\": \"成功\"}";
        } else {
            return "{\"code\": \"0\", \"msg\": \"密码错误\"}";
        }
    }

    @ApiOperation(value = "获取有效ad", notes = "")
    @RequestMapping(value = "/findUsers", method = { RequestMethod.GET })
    public BaseResult findUsers() throws Exception {
        List<LdapUser> alls = adService.getLdapUser(null, "CN=Person,CN=Schema,CN=Configuration,DC=pechoin,DC=com");
        log.info("ad findUserByName===="+ alls);
        return new BaseResult(alls);
    }

    /**
     * 查询最新软件版本
     */
    @ApiOperation(value = "查询最新软件版本", notes = "")
    @RequestMapping(value = "/getNewSoftwareVersion", method = { RequestMethod.POST })
    public BaseResult getNewSoftwareVersion() {
        return new BaseResult("2.0");
    }
    
    /**
     * Rsa加解密，公钥加密，私钥解密
     */
    @ApiOperation(value = "Rsa加解密", notes = "")
    @RequestMapping(value = "/rsaTest", method = { RequestMethod.GET })
    public BaseResult rsaTest() {
        try {
            //生成RSA公钥和私钥，并Base64编码
            KeyPair keyPair = RsaUtil.getKeyPair();
            String publicKeyStr = RsaUtil.getPublicKey(keyPair);
            String privateKeyStr = RsaUtil.getPrivateKey(keyPair);
            System.out.println("RSA公钥Base64编码:" + publicKeyStr);
            System.out.println("RSA私钥Base64编码:" + privateKeyStr);
            
            String message = "hello, i am infi, good night!";
            
            //将Base64编码后的公钥转换成PublicKey对象
            PublicKey publicKey = RsaUtil.string2PublicKey(publicKeyStr);
            //用公钥加密
            byte[] publicEncrypt = RsaUtil.publicEncrypt(message.getBytes(), publicKey);
            //加密后的内容Base64编码
            String byte2Base64 = RsaUtil.byte2Base64(publicEncrypt);
            System.out.println("公钥加密并Base64编码的结果：" + byte2Base64);
            
            //将Base64编码后的私钥转换成PrivateKey对象
            PrivateKey privateKey = RsaUtil.string2PrivateKey(privateKeyStr);
            //加密后的内容Base64解码
            byte[] base642Byte = RsaUtil.base642Byte(byte2Base64);
            //用私钥解密
            byte[] privateDecrypt = RsaUtil.privateDecrypt(base642Byte, privateKey);
            //解密后的明文
            System.out.println("解密后的明文: " + new String(privateDecrypt));
        } catch (Exception e) {
            log.error("Error Exception=", e);
        }

        return new BaseResult();
    }
    
    /**
     * Rsa加解密，私钥加密，公钥解密
     */
    @ApiOperation(value = "Rsa加解密", notes = "")
    @RequestMapping(value = "/rsaTest2", method = { RequestMethod.GET })
    public BaseResult rsaTest2() {
        try {
            //生成RSA公钥和私钥，并Base64编码
            KeyPair keyPair = RsaUtil.getKeyPair();
            String publicKeyStr = RsaUtil.getPublicKey(keyPair);
            String privateKeyStr = RsaUtil.getPrivateKey(keyPair);
            System.out.println("RSA公钥Base64编码:" + publicKeyStr);
            System.out.println("RSA私钥Base64编码:" + privateKeyStr);
            
            String message = "hello, i am infi, good night!";
            
            //将Base64编码后的私钥转换成PrivateKey对象
            PrivateKey privateKey = RsaUtil.string2PrivateKey(privateKeyStr);
            //用私钥加密
            byte[] privateEncrypt = RsaUtil.privateEncrypt(message.getBytes(), privateKey);
            //加密后的内容Base64编码
            String byte2Base64 = RsaUtil.byte2Base64(privateEncrypt);
            System.out.println("私钥加密并Base64编码的结果：" + byte2Base64);
            
            //将Base64编码后的公钥转换成PublicKey对象
            PublicKey publicKey = RsaUtil.string2PublicKey(publicKeyStr);
            //加密后的内容Base64解码
            byte[] base642Byte = RsaUtil.base642Byte(byte2Base64);
            //用公钥解密
            byte[] publicDecrypt = RsaUtil.publicDecrypt(base642Byte, publicKey);
            //解密后的明文
            System.out.println("解密后的明文: " + new String(publicDecrypt));
        } catch (Exception e) {
            log.error("Error Exception=", e);
        }

        return new BaseResult();
    }
    
    /**
     * Rsa加解密，私钥加密，公钥解密，固定公钥、私钥
     */
    @ApiOperation(value = "Rsa加解密", notes = "")
    @RequestMapping(value = "/rsaTest3", method = { RequestMethod.GET })
    public BaseResult rsaTest3() {
        try {
            //生成RSA公钥和私钥，并Base64编码
            String publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgu234D2jvmMn8OGiOumUcfyyg+Er/GyghfrAdq6mpFQ7l6de85RrGzv4iT6LrlrzkOEzTaR0n0z0UrwLHdzTEYB2TLF66Fx/lpErtgKlhfTSrVDrIyPJwvGBk6KKEPFfVkc3oaMZ6sr8HyyjfMX9ML+elkNnCgTLo/frkwGxG05ARPKuETxwURZutBkIsXgBDhIaDqGR4lHj7dgvK2wVweOj6Jr+bUhWwdNW+Qyi49DSNQPwsi3AUmuz0lSWaevgpyAW+zOfxs02e5WXqbIlRWMpVsJfb6+qbHE6briCy2Y4VAtR08kiDcm8R8M4qL5fEBLtQ5oMKqyAQ6hKStiTLwIDAQAB";
            String privateKeyStr = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCC7bfgPaO+Yyfw4aI66ZRx/LKD4Sv8bKCF+sB2rqakVDuXp17zlGsbO/iJPouuWvOQ4TNNpHSfTPRSvAsd3NMRgHZMsXroXH+WkSu2AqWF9NKtUOsjI8nC8YGToooQ8V9WRzehoxnqyvwfLKN8xf0wv56WQ2cKBMuj9+uTAbEbTkBE8q4RPHBRFm60GQixeAEOEhoOoZHiUePt2C8rbBXB46Pomv5tSFbB01b5DKLj0NI1A/CyLcBSa7PSVJZp6+CnIBb7M5/GzTZ7lZepsiVFYylWwl9vr6pscTpuuILLZjhUC1HTySINybxHwziovl8QEu1DmgwqrIBDqEpK2JMvAgMBAAECggEAP0aV3AjOLwAK2I6jcedbN6+RsszLDkaNWdLy2yjTHo4a8tMPv5aWIOZd2i5hIVWRaw57VeXJ+wcc+TMqCDelxkqYYb5QkSXK/8CPKHqk9lV40AxQZ34vT0iaf/hI06FjmvX2O+bwapnJOWPAa1fgYoPpJ+PRU3U32bnCnxxtNK1uvRl7p9IlK29exh6QAJyNAtB9A6GKLPeVsg9LRr/pDdpirQN6/9NQZjlQ39ZKn+9Cj9E1U4AebsYYsLybwAW2survfJchKELsBs4r1NGQ65QagqrIpSPRGqsSAgNnD8l9Mvu4gbwgQqu4FDM2RUTYNIexm0ovH46EMTxwLBSnWQKBgQC7imua82bGJ65/yzH7APgtzGEu9laBxRLGro93YuITkM2XuzAIdmldEV18ryooXXHzuh5Lv3QtDZAlheNHjJK0oSJmcwvtH7JqC+QpPentpcHQGS4gPangThdBLr7gf5Y0PfVSX89gPgxDFdUUcHkEygxAAXsHv2SeHRzbXwfavQKBgQCyuOzs6FyergVU/S3XAE96avkSstoopAzLdEgd+KMZOJEqWsasg+0Hdzzm5W7QHiNs962Q7g7CHdaz2gf0ojAln/3ZE/Z6Fmz0sJg/uEnppkxX3kyRYTzGACdKUVEq+KV0AIvlCyOmMeUzjlozs8F/WGowRvsBhxBG9WvwsOs6WwKBgBSm9JyWqz2tBwFZrgJXI/1fIWCbjgEyY5Pviyr5f63p50oLOgnkiEvQarwdSgYx7CeL2mQ7DB8d/D61lMa/SzSK/g/MIYcWU35sGs8T21vpQgOZkkPIppll4Be0fx6XzkGohQhyNpMBojYNBG8Ax8LoQRsXTqYWxIzI5fGecG39AoGANHA37Ab9bf2HSPmEIqpPp46NiCP8CKKhsmxq4IhcaoEDbtb1phaLTH562xCEZAIDyOwe/AAtSl1w6pJVdTzFPbTkyXWnAwljbKQH9+I9pZWKntl1w84xA7N108NqOl58RT4HzetnyVKoemkRJExgwNmTRpj8+uTDLN7DjfqK+jMCgYEAtywIWcyn+2PF8qFmK0ggxTR9TLqIiuqTZWCVOZ7Us0tPO0XAuxa/jCSccMxwdMHYvi+SgKKp4RI2BmhD0EVTYUssNcP1hdo10ZyLXbVp4Xl7JyvC/Iqw8zt9u81x5iENt6FQO7tlKNpInoBXLcVzMlbDqEhUaVSaCpQm57DIkp0=";
            System.out.println("RSA公钥Base64编码:" + publicKeyStr);
            System.out.println("RSA私钥Base64编码:" + privateKeyStr);
            
            String message = "hello, i am infi, good night!";
            
            //将Base64编码后的私钥转换成PrivateKey对象
            PrivateKey privateKey = RsaUtil.string2PrivateKey(privateKeyStr);
            //用私钥加密
            byte[] privateEncrypt = RsaUtil.privateEncrypt(message.getBytes(), privateKey);
            //加密后的内容Base64编码
            String byte2Base64 = RsaUtil.byte2Base64(privateEncrypt);
            System.out.println("私钥加密并Base64编码的结果：" + byte2Base64);
            
            //将Base64编码后的公钥转换成PublicKey对象
            PublicKey publicKey = RsaUtil.string2PublicKey(publicKeyStr);
            //加密后的内容Base64解码
            byte[] base642Byte = RsaUtil.base642Byte(byte2Base64);
            //用公钥解密
            byte[] publicDecrypt = RsaUtil.publicDecrypt(base642Byte, publicKey);
            //解密后的明文
            System.out.println("解密后的明文: " + new String(publicDecrypt));
        } catch (Exception e) {
            log.error("Error Exception=", e);
        }

        return new BaseResult();
    }
    
    /**
     * Rsa加解密，公钥加密，私钥解密，固定公钥、私钥
     */
    @ApiOperation(value = "Rsa加解密", notes = "")
    @RequestMapping(value = "/rsaTest4", method = { RequestMethod.GET })
    public BaseResult rsaTest4() {
        try {
            String message = "hello, i am infi, good night!";
            
            // 公钥加密
            String pubEncrypt = RSAEncryption.encryptByPubKey(message);
            System.out.println("公钥加密:" + pubEncrypt);
            
            // 私钥解密
            String priDecrypt = RSAEncryption.decryptByPriKey(pubEncrypt);
            System.out.println("私钥解密:" + priDecrypt);

        } catch (Exception e) {
            log.error("Error Exception=", e);
        }

        return new BaseResult();
    }
    
    /**
     * 生成大量测试数据
     */
//    @ApiOperation(value = "生成大量测试数据", notes = "")
//    @RequestMapping(value = "/addTestData", method = { RequestMethod.GET })
    public BaseResult addTestData() {
        for (int i = 0; i < 15000; i++) {
            String userName = StringUtil.getRandomString(10);
            String userIp = StringUtil.getRandomString(10);
            String userMac = StringUtil.getRandomString(12);
            String acIp = "172.16.10.253";
            String acMac = StringUtil.getRandomString(12);
            Integer acId = 1;
            
            Employee employee = new Employee();
            employee.setDepartmentId(3);
            employee.setFullName(userName);
            employee.setUserName(userName);
            employee.setPassword("123456");
            employee.setPhone("15800002222");
            employee.setBandwidthId(7);
            employee.setTerminalNum(3);
            employee.setIsTerminalNumLimit(0);
            employee.setIsBindMac(0);
            employee.setIsEmployeeAuthEnable(1);
            employee.setIsUsing(1);
            employee.setIsValid(1);
            employeeService.save(employee);
            
            AuthParam authParam = new AuthParam();
            authParam.setUserName(userName);
            authParam.setPassword("123456");
            authParam.setUserIp(userIp);
            authParam.setUserMac(userMac);
            authParam.setAcIp(acIp);
            authParam.setAcMac(acMac);
            authParam.setAcId(acId);
            authParam.setAuthMethod(1);
            authParam.setTerminalType(1);
            authParam.setIsValid(1);          
            authParamService.save(authParam);
            
            AuthRecord authRecord = new AuthRecord();
            authRecord.setAuthMethod(1);
            authRecord.setUserType(0);
            authRecord.setFullName(userName);
            authRecord.setUserName(userName);
            authRecord.setIp(userIp);
            authRecord.setMac(userMac);
            authRecord.setBandwidthId(7);
            authRecord.setAcId(1);
            authRecord.setLastOnlineTime(new Date());
            authRecord.setOnlineState(0);
            authRecord.setIsValid(1);
            authRecordService.save(authRecord);
            
            AuthUser authUser = new AuthUser();
            authUser.setAuthMethod(1);
            authUser.setUserType(0);
            authUser.setFullName(userName);
            authUser.setUserName(userName);
            authUser.setIp(userIp);
            authUser.setMac(userMac);
            authUser.setBandwidthId(7);
            authUser.setAcId(1);
            authUser.setLastOnlineTime(new Date());
            authUser.setOnlineState(0);
            authUser.setIsValid(1);
            authUserService.save(authUser);
        }
        return new BaseResult();
    }

    /**
     * 模板导入
     */
    @ApiOperation(value="模板导入", notes="")
    @RequestMapping(value="/excelImport", method={RequestMethod.POST})
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

            // 文件内部不能有重复的账号
            List<Employee> employeeList = new ArrayList<>();
            List<AuthParam> authParamList = new ArrayList<>();
            Employee employee = null;
            AuthParam authParam = null;
            for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                // 忽略空白行
                if (row == null) {
                    if(null!=employee){
                        employeeList.add(employee);
                        employee = null;
                    }
                    continue;
                }
                if(null == employee) {
                    employee = new Employee();
                }

                if(StringUtils.isNotBlank(employee.getUserName())){
                    if(!employee.getUserName().equals(fillUserName(getCellValue(row.getCell(6))))) {
                        employeeList.add(employee);
                        employee = new Employee();
                        employee.setFullName(getCellValue(row.getCell(5)));
                        employee.setUserName(fillUserName(getCellValue(row.getCell(6))));
                        employee.setPassword("123456");
                        employee.setDepartmentId(1);
                        employee.setBandwidthId(8);
                        employee.setTerminalNum(15);
                        employee.setIsBindMac(1);
                        employee.setBindMacs(getCellValue(row.getCell(1)));
                        employee.setPhone(getCellValue(row.getCell(7)));
                        employee.setSex((int)(Math.random()*2));
                        employee.setIsEmployeeAuthEnable(1);
                        employee.setIsValid(1);
                        employee.setIsUsing(1);
                    } else {
                        employee.setBindMacs(employee.getBindMacs()+","+getCellValue(row.getCell(1)));
                    }
                } else {
                    employee.setFullName(getCellValue(row.getCell(5)));
                    employee.setUserName(fillUserName(getCellValue(row.getCell(6))));
                    employee.setPassword("123456");
                    employee.setDepartmentId(1);
                    employee.setBandwidthId(8);
                    employee.setTerminalNum(15);
                    employee.setIsBindMac(1);
                    employee.setBindMacs(getCellValue(row.getCell(1)));
                    employee.setPhone(getCellValue(row.getCell(7)));
                    employee.setSex((int)(Math.random()*2));
                    employee.setIsEmployeeAuthEnable(1);
                    employee.setIsValid(1);
                    employee.setIsUsing(1);
                }

                //增加authParam
                authParam = new AuthParam();
                authParam.setUserName(fillUserName(getCellValue(row.getCell(6))));
                authParam.setUserMac(getCellValue(row.getCell(1)));
                authParam.setAuthMethod(1);
                authParam.setIsModify(1);
                authParam.setIsValid(1);
                authParam.setIsWired(0);
                authParam.setAcIp("1.1.1.1");
                authParam.setAcId(7);
                authParam.setNasIp("192.168.1.100");
                authParam.setUserVisitUrl("http://www.baidu.com");

                authParamList.add(authParam);
            }

            // 存入数据库
            employeeService.saveBatch(employeeList);
            log.info("====批量导入用户成功");
            authParamService.saveBatch(authParamList);
            log.info("====批量导入authParam成功");
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

    private String fillUserName(String s){
        if(StringUtils.isNotBlank(s)){
            int len = 4-s.length();
            for (int i = 0; i < len; i++) {
                s = "0" + s;
            }
        }
        return s;
    }
    
}
