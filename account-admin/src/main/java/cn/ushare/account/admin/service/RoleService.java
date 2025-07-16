package cn.ushare.account.admin.service;

import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.Role;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface RoleService extends IService<Role> {

    BaseResult add(Role role);

    BaseResult update(Role role);

    BaseResult delete(Integer id);

    BaseResult getInfo(Integer id);

    Page<Role> getList(Page<Role> page, QueryWrapper wrapper);

}
