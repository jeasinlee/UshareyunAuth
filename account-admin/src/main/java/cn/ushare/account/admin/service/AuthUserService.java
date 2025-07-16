package cn.ushare.account.admin.service;

import cn.ushare.account.entity.AuthUser;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @since 2019-05-03
 * @email jixiang.li@ushareyun.net
 */
public interface AuthUserService extends IService<AuthUser> {

    BaseResult saveOrUpdateByMac(AuthUser authUser);

    BaseResult updateByMac(AuthUser authUser);

    BaseResult resetFlow(String userIp, String userMac);

    BaseResult offline(Long id);

    BaseResult updateOfflineState(String userMac);

    Page statisticNewUserDaily(Page page);

    Page<AuthUser> getList(Page<AuthUser> page, QueryWrapper wrapper);

}
