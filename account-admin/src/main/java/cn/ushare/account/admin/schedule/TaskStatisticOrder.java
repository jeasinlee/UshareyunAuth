package cn.ushare.account.admin.schedule;

import cn.ushare.account.admin.service.AccountOrdersService;
import cn.ushare.account.admin.service.AccountSalesStatisticService;
import cn.ushare.account.entity.AccountOrders;
import cn.ushare.account.entity.AccountSalesStatistic;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 定时统计昨日订单
 */
@Configuration
@EnableScheduling
@Slf4j
public class TaskStatisticOrder {

	@Autowired
    AccountOrdersService ordersService;
    @Autowired
    AccountSalesStatisticService statisticService;
	@Value("${schedule.statisticOrderTime}")
    String statisticOrderTime;

	private static boolean inProcess = false;

	@Scheduled(cron = "${schedule.statisticOrderTime}")
    public void scheduler() throws Exception {
	    log.debug("statisticOrder " + statisticOrderTime);
	    if (inProcess) {
            return;
        }
        inProcess = true;
        try {

            QueryWrapper<AccountOrders> wrapper = new QueryWrapper<>();
            wrapper.apply("DATEDIFF(`create_time`,NOW())={0}", -1);
            List<AccountOrders> orders = ordersService.list(wrapper);
            List<AccountOrders> ordersSuccess = orders.stream().filter(
                    o -> ((null!= o.getOrderStatus() && 1 == o.getOrderStatus()) && (null!=o.getChargeStatus() && 1 == o.getChargeStatus()))).collect(Collectors.toList());
            Integer orderSuccessAmount = ordersSuccess.stream().mapToInt(AccountOrders::getTotalFee).sum();

            List<AccountOrders> totalAliOrders = ordersSuccess.stream().filter(
                    o -> (0==o.getPayType() && (null!= o.getOrderStatus() && 1 == o.getOrderStatus()) && (null!=o.getChargeStatus() && 1 == o.getChargeStatus()))).collect(Collectors.toList());
            List<AccountOrders> totalWeixinOrders = ordersSuccess.stream().filter(
                    o -> (1==o.getPayType() && (null!= o.getOrderStatus() && 1 == o.getOrderStatus()) && (null!=o.getChargeStatus() && 1 == o.getChargeStatus()))).collect(Collectors.toList());

            Date now = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String dayStr = format.format((new DateTime(now).plusDays(-1)).toDate());

            AccountSalesStatistic salesStatistic = new AccountSalesStatistic();
            salesStatistic.setDayStr(dayStr);
            salesStatistic.setTotalAmount(orderSuccessAmount);
            salesStatistic.setTotalNum(orders.size());
            salesStatistic.setTotalNumSuccess(ordersSuccess.size());
            salesStatistic.setTotalNumAli(totalAliOrders.size());
            salesStatistic.setTotalNumWeixin(totalWeixinOrders.size());

            salesStatistic.setTotalNumRefund(0);
            statisticService.save(salesStatistic);

            log.error("定时统计订单数量=====" + orders.size());
        } catch (Exception e) {
            log.error("Error Exception=", e);
            inProcess = false;
        }
    	inProcess = false;
    }

}
