package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.AuthQrcode;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AuthQrcodeMapper extends BaseMapper<AuthQrcode> {
    @Select("SELECT qr.id, qr.ac_id, qr.is_default, qr.start_time, qr.end_time, qr.bandwidth_id," +
            " qr.sn, qr.url, qr.supply_user_info, qr.image_url, qr.remark, qr.update_time, " +
            "ac.name as acName, bd.name as bandwidthName FROM auth_qrcode qr" +
            " LEFT JOIN ac ON (qr.ac_id = ac.id AND ac.is_valid = 1)" +
            " LEFT JOIN bandwidth bd ON (qr.bandwidth_id = bd.id AND bd.is_valid = 1) " +
            " ${ew.customSqlSegment}")
    List<AuthQrcode> getList(Page<AuthQrcode> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Select("SELECT id, ac_id, sn, url, image_url, remark, supply_user_info" +
            " FROM auth_qrcode WHERE sn = #{sn} AND is_valid = 1")
    AuthQrcode getValidCode(String sn);

}
