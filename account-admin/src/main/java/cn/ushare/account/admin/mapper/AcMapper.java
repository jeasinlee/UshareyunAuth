package cn.ushare.account.admin.mapper;

import cn.ushare.account.entity.Ac;
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
public interface AcMapper extends BaseMapper<Ac> {
    @Select("SELECT ac.*, br.name as brandName, br.code as brandCode, mo.name as modelName, " +
            " tp.name as authTemplateName FROM ac" +
            " LEFT JOIN device_brand br" +
            "   ON (ac.brand_id = br.id AND br.is_valid = 1)" +
            " LEFT JOIN device_model mo" +
            "   ON (ac.model_id = mo.id AND mo.is_valid = 1)" +
            " LEFT JOIN auth_template tp" +
            "   ON (ac.auth_template_id = tp.id AND tp.is_valid = 1) ${ew.customSqlSegment}")
    List<Ac> getList(Page<Ac> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Select("SELECT ac.id, ac.name, ac.brand_id, ac.model_id, ac.portal_version,\n" +
            "            ac.share_key, ac.ip, ac.port, ac.auth_type, ac.auth_template_id,\n" +
            "            ac.auth_method, ac.wx_shop_config_id, ac.ding_talk_config_id, ac.expire_time,\n" +
            "            ac.is_pc_enable, ac.is_whitelist_enable, ac.nas_ip, ac.send_once,\n" +
            "            ac.portal_url, br.name AS brandName, br.code AS brandCode\n" +
            "        FROM ac\n" +
            "        LEFT JOIN device_brand br ON (ac.brand_id = br.id AND br.is_valid = 1)\n" +
            "        WHERE ac.id = #{id}")
    Ac getInfo(Integer id);

}
