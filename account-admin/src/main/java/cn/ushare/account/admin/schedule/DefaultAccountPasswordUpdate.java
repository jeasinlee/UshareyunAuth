package cn.ushare.account.admin.schedule;

import cn.ushare.account.admin.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.ushare.account.entity.Employee;
import cn.ushare.account.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
 
 
/**
 * 定时更新免验证默认账户的密码
 */
@Configuration
@EnableScheduling
@Slf4j
public class DefaultAccountPasswordUpdate {
	 
	@Autowired
    EmployeeService employeeService;
	
	@Scheduled(cron = "${schedule.defaultPasswordUpdateTime}")
    public void scheduler() throws Exception {
	    // 查询
	    QueryWrapper<Employee> query = new QueryWrapper();
	    query.eq("is_valid", 1);
	    query.eq("user_name", "portalDefaultAccount");
	    Employee employee = employeeService.getOne(query);
	    // 更新
	    log.debug("update default account password");
	    employee.setPassword(StringUtil.getRandomString(12));
	    employeeService.updateById(employee);
	    
	    // 查询微信默认账号
        QueryWrapper<Employee> wxQuery = new QueryWrapper();
        wxQuery.eq("is_valid", 1);
        wxQuery.eq("user_name", "portalDefaultWxAccount");
        Employee wxEmployee = employeeService.getOne(wxQuery);
        // 更新
        wxEmployee.setPassword(StringUtil.getRandomString(12));
        employeeService.updateById(wxEmployee);
        
        // 查询钉钉默认账号
        QueryWrapper<Employee> dingQuery = new QueryWrapper();
        dingQuery.eq("is_valid", 1);
        dingQuery.eq("user_name", "portalDefaultDingTalkAccount");
        Employee dingEmployee = employeeService.getOne(dingQuery);
        // 更新
        dingEmployee.setPassword(StringUtil.getRandomString(12));
        employeeService.updateById(dingEmployee);
    }
	
}
