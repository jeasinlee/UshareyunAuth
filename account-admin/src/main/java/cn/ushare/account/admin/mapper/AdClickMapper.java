package cn.ushare.account.admin.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.ushare.account.entity.AdClick;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AdClickMapper extends BaseMapper<AdClick> {

    @Select("SELECT im.`name`, count(ck.ad_image_id) AS num FROM ad_image im" +
            " LEFT JOIN ad_click ck ON im.id = ck.ad_image_id" +
            " AND date_format(ck.create_time, '%Y%m%d') = date_format(sysdate(), '%Y%m%d') " +
            " WHERE im.is_valid = 1 GROUP BY im.`name`")
    List<Map<String, Integer>> statisticToday();

    @Select("SELECT im.`name`, count(ck.ad_image_id) AS num FROM ad_image im" +
            " LEFT JOIN ad_click ck ON" +
            " im.id = ck.ad_image_id AND TO_DAYS(NOW()) - TO_DAYS(ck.create_time)  &lt;= #{days}" +
            " WHERE im.is_valid = 1 GROUP BY im.`name` ORDER BY num desc LIMIT 5")
    List<Map<String, Integer>> statisticInDays(Integer days);

    @Select("SELECT * FROM ad_click ${ew.customSqlSegment}")
    List<AdClick> getList(Page<AdClick> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

}
