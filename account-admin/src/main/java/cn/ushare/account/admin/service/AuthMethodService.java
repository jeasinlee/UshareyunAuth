package cn.ushare.account.admin.service;

import cn.ushare.account.entity.AuthMethod;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AuthMethodService extends IService<AuthMethod> {

    Page<AuthMethod> getList(Page<AuthMethod> page, QueryWrapper wrapper);

}
