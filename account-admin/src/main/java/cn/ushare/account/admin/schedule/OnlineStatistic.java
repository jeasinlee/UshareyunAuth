package cn.ushare.account.admin.schedule;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.ushare.account.admin.service.AuthUserService;
import cn.ushare.account.admin.service.OnlineUserStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import cn.ushare.account.admin.portal.service.IdentityCheckService;
import cn.ushare.account.dto.AuthLogoutParam;
import cn.ushare.account.entity.Ac;
import cn.ushare.account.entity.AuthUser;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.Employee;
import cn.ushare.account.entity.OnlineUserStatistic;
import cn.ushare.account.util.DateTimeUtil;
import cn.ushare.account.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
 
 
/**
 * 在线用户统计
 */
@Configuration
@EnableScheduling
@Slf4j
public class OnlineStatistic {
	 
	@Autowired
    AuthUserService authUserService;
	@Autowired
    OnlineUserStatisticService statisticService;
	@Autowired
	IdentityCheckService identityCheckService;
	
	@Scheduled(cron = "${schedule.onlineStatisticTime}")
    public void scheduler() throws Exception {
	    // 查询在线用户
	    QueryWrapper<AuthUser> query = new QueryWrapper();
	    query.eq("online_state", 1);
	    query.eq("is_valid", 1);
	    Integer userNum = authUserService.count(query);
	    
	    // 是否已有相同时间的记录
	    Date currentTime = new Date();
	    SimpleDateFormat sf = new SimpleDateFormat("HH:mm");
        String curTimeStr = sf.format(currentTime);
	    QueryWrapper<OnlineUserStatistic> queryWrapper = new QueryWrapper();
	    queryWrapper.eq("record_time", curTimeStr);
		queryWrapper.apply("TO_DAYS(create_time)=TO_DAYS(now())");
	    OnlineUserStatistic statistic = statisticService.getOne(queryWrapper);
	    if (statistic == null) {
	        statistic = new OnlineUserStatistic();
	        statistic.setRecordTime(curTimeStr);
	        statistic.setUserNum(userNum);
	        statisticService.save(statistic);
	    } else {
            statistic.setRecordTime(curTimeStr);
            statistic.setUserNum(userNum);
            statisticService.updateById(statistic);
	    }
    }
	
}
