package cn.ushare.account.admin.service;

import cn.ushare.account.entity.AuthParam;
import cn.ushare.account.entity.AuthRecord;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AuthRecordService extends IService<AuthRecord> {

    BaseResult add(AuthParam authParam, boolean macPrior);

    BaseResult statisticAuthMethod();

    BaseResult statisticTerminalType();

    BaseResult statisticAuthUserNum(Integer type);

    AuthRecord getTopOne(String userIp);

    Long countToday();

    BaseResult updateAcctSessionId(String userIp, String acctSessionId);

    BaseResult updateAccountInfo(String userIp, String userMac, Long upFlow,
            Long downFlow, String acctSessionId, Integer state);

    Page<AuthRecord> getList(Page<AuthRecord> page, QueryWrapper wrapper);

    List<AuthRecord> statisticFlowTop(Integer type);

    List<AuthRecord> statisticPeriodTop(Integer type);

}
