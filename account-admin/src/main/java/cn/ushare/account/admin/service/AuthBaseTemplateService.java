package cn.ushare.account.admin.service;

import cn.ushare.account.entity.AuthBaseTemplate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-03-27
 * @email jixiang.li@ushareyun.net
 */
public interface AuthBaseTemplateService extends IService<AuthBaseTemplate> {

    Page<AuthBaseTemplate> getList(Page<AuthBaseTemplate> page, QueryWrapper wrapper);

}
