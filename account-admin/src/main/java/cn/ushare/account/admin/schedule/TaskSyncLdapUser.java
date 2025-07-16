package cn.ushare.account.admin.schedule;

import cn.ushare.account.admin.service.AdService;
import cn.ushare.account.admin.service.EmployeeService;
import cn.ushare.account.dto.LdapUser;
import cn.ushare.account.entity.Employee;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 系统定时同步ldap用户信息
 */
@Configuration
@EnableScheduling
@Slf4j
public class TaskSyncLdapUser {

    @Autowired
    @Qualifier("adService")
    AdService adService;
	@Autowired
    EmployeeService employeeService;
	
	@Scheduled(cron = "${schedule.syncLdapTime}")
    public void scheduler() throws Exception {
        syncUser();
    }

    /**
     * 同步ldap用户
     */
    void syncUser() {
        //数据库存在的数据
        List<LdapUser> allUsers = adService.getLdapUser(null, "CN=Person,CN=Schema,CN=Configuration,DC=pechoin,DC=com");
        List<String> allUids = allUsers.stream().map(LdapUser::getUid).collect(Collectors.toList());

        List<Employee> employeeList = employeeService.list(new QueryWrapper<Employee>().gt("id", 3));
        ArrayList<Integer> deleteIds = new ArrayList<>();
        //同步删除信息
        for (int i = 0; i < employeeList.size(); i++) {
            if(!allUids.contains(employeeList.get(i).getUserName())){
                deleteIds.add(employeeList.get(i).getId());
            }
        }
        log.info("=====删除数量：" + deleteIds.size());
        if(deleteIds.size()>0) {
            employeeService.removeByIds(deleteIds);
        }
        log.info("=====同步删除用户成功.");
    }
}
