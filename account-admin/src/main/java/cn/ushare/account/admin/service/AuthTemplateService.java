package cn.ushare.account.admin.service;

import cn.ushare.account.entity.AuthTemplate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @since 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
public interface AuthTemplateService extends IService<AuthTemplate> {

    Page<AuthTemplate> getList(Page<AuthTemplate> page, QueryWrapper wrapper);

    AuthTemplate getInfo(Integer id);
}
