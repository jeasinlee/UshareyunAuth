package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.AccountUser;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author jixiang.li
 * @since 2022-04-02
 * @email jixiang.li@ushareyun.net
 */
public interface AccountUserMapper extends BaseMapper<AccountUser> {
    @Select("SELECT au.*, acp.band_id AS bandId, acp.band_name AS bandName, acp.policy_name AS chargePolicyName " +
            " FROM account_user au LEFT JOIN account_charge_policy acp " +
            " ON au.charge_policy_id=acp.id ${ew.customSqlSegment}")
    List<AccountUser> getList(Page<AccountUser> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Select("SELECT id, login_name, account_group_name, locked_reason, update_time " +
            " FROM account_user ${ew.customSqlSegment}")
    List<AccountUser> getLockedList(Page<AccountUser> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Select("SELECT id, login_name, account_group_name, update_time FROM account_user ${ew.customSqlSegment}")
    List<AccountUser> getDebtList(Page<AccountUser> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Select("SELECT au.*, acp.band_id AS bandId, acp.band_name AS bandName, acp.policy_name AS chargePolicyName " +
            " FROM account_user au LEFT JOIN account_charge_policy acp ON au.charge_policy_id=acp.id " +
            " WHERE au.login_name=#{loginName} AND au.is_valid=#{isValid}")
    AccountUser getDetail(String loginName, int isValid);

    @Update("<script>" +
            "UPDATE account_user SET is_valid=0 WHERE id IN" +
            "   <foreach collection='list' item='id' index='index' open='(' close=')' separator=','>" +
            "      #{id}" +
            "   </foreach>" +
            "</script>")
    void updateByIds(List<Integer> ids);
}
