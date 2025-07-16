package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.AccountChargeRecord;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AccountChargeRecordMapper extends BaseMapper<AccountChargeRecord> {
    @Select("SELECT * FROM account_charge_record ${ew.customSqlSegment}")
    List<AccountChargeRecord> getList(Page<AccountChargeRecord> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Select("SELECT * FROM account_charge_record WHERE order_num=#{orderNum}")
    AccountChargeRecord getByOrderNum(String orderNum);
}
