package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.Ap;
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
public interface ApMapper extends BaseMapper<Ap> {
    @Select("SELECT ap.id, ap.ac_id, IFNULL(ap.name, '--') as name, " +
            " IFNULL(ap.ip, '--') as ip, ap.mac, IFNULL(ap.location, '--') as location, " +
            " ap.auth_num, ap.update_time, ac.name as acName FROM ap" +
            " LEFT JOIN ac ON (ap.ac_id = ac.id AND ac.is_valid = 1) ${ew.customSqlSegment}")
    List<Ap> getList(Page<Ap> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
