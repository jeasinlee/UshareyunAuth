package cn.ushare.account.admin.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.ushare.account.entity.Ssid;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface SsidMapper extends BaseMapper<Ssid> {

    @Select("SELECT s.*, d.name as departmentName, tp.name as authTemplateName, " +
            "   acBr.brandCode AS brandCode FROM ssid s" +
            "   LEFT JOIN department d" +
            "       ON s.department_id = d.id" +
            "   LEFT JOIN (select ac.id AS acId, br.code AS brandCode FROM `device_brand` br, `ac` ac " +
            "       WHERE ac.brand_id=br.id) AS acBr" +
            "       ON s.ac_id = acBr.acId" +
            "   LEFT JOIN auth_template tp" +
            "       ON (s.auth_template_id = tp.id AND tp.is_valid = 1) ${ew.customSqlSegment}")
    List<Ssid> getList(Page<Ssid> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

    @Update("UPDATE ssid SET department_id = null WHERE id = #{id}")
    Integer setDepartmentIdNull(Integer id);

}
