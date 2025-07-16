package cn.ushare.account.admin.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.ushare.account.entity.Department;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface DepartmentMapper extends BaseMapper<Department> {

	@Update("UPDATE department SET bandwidth_id = null WHERE id = #{id}")
    Integer setBandwidthNull(Integer id);

	@Select("SELECT d.id, d.parent_id, d.NAME, d.bandwidth_id,  d.is_employee_auth_enable, " +
			" d.is_ad_domain_enable, d.state, d.update_time, IFNULL(pd.NAME, \"\") AS parentName" +
			" FROM department d LEFT JOIN department pd ON (" +
			" d.parent_id = pd.id AND pd.is_valid = 1) ${ew.customSqlSegment}")
    List<Department> getList(Page<Department> page, @Param(Constants.WRAPPER) QueryWrapper wrapper);

	@Select("SELECT count(1) from employee WHERE is_valid = 1" +
			" AND FIND_IN_SET(department_id, queryChildrenDepartment(#{id}))")
	Integer countEmployeeByDepartment(Integer id);

	@Select("SELECT queryChildrenDepartment(#{id})")
	String getChildrenIds(Integer id);
}
