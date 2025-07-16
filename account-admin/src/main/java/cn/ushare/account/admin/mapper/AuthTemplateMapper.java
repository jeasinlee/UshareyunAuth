package cn.ushare.account.admin.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.ushare.account.entity.AuthTemplate;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author jixiang.li
 * @since 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
public interface AuthTemplateMapper extends BaseMapper<AuthTemplate> {

    @Select("SELECT t.id, t.base_template_id, t.`name`, t.company_name, t.welcome," +
            " t.logo_url, t.bg_image_url, t.mobile_bg_image_url, t.banner_image_ids, t.is_open, t.terminal_type," +
            " t.update_time, bt.url AS templateUrl, bt.mobile_url AS templateMobileUrl" +
            " FROM auth_template t LEFT JOIN auth_base_template bt " +
            " ON t.base_template_id=bt.id ${ew.customSqlSegment}")
    List<AuthTemplate> getList(Page<AuthTemplate> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
