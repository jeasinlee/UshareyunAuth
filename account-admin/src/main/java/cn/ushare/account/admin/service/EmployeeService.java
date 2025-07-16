package cn.ushare.account.admin.service;

import cn.ushare.account.dto.EmployeeChangePwdReq;
import cn.ushare.account.dto.EmployeeFirstModifyPwdReq;
import cn.ushare.account.dto.EmployeeGetSmsReq;
import cn.ushare.account.dto.LoginGetSmsReq;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.Employee;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface EmployeeService extends IService<Employee> {

    BaseResult setBandwidthNull(Integer id);

    BaseResult changePwd(EmployeeChangePwdReq param) throws Exception;

    BaseResult firstModifyPwd(EmployeeFirstModifyPwdReq param) throws Exception;

    BaseResult loginGetSmsCode(LoginGetSmsReq param) throws Exception;

    BaseResult changePwdGetSmsCode(EmployeeGetSmsReq param) throws Exception;

    BaseResult excelImport(MultipartFile file) throws Exception;

    void excelExport(String ids) throws Exception;

    Page<Employee> getList(Page<Employee> page, QueryWrapper wrapper);

    Integer getDepartmentBandwidth(String userName);

}
