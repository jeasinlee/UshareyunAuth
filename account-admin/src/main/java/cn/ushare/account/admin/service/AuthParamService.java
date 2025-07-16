package cn.ushare.account.admin.service;

import cn.ushare.account.entity.AuthParam;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author jixiang.li
 * @date 2019-05-02
 * @email jixiang.li@ushareyun.net
 */
public interface AuthParamService extends IService<AuthParam> {

    BaseResult addOrUpdateByIp(AuthParam authParam) throws Exception;

    BaseResult addOrUpdateByMac(AuthParam authParam) throws Exception;

    AuthParam getByUserIp(String ip);

    AuthParam getByUserMac(String mac);

    AuthParam getByUserMacAndAcip(String mac, String acIp);

    Page<AuthParam> getList(Page<AuthParam> page, QueryWrapper wrapper);

}
