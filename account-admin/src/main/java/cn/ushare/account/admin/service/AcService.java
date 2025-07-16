package cn.ushare.account.admin.service;

import java.util.Map;
import cn.ushare.account.entity.Ac;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.DingTalkConfig;
import cn.ushare.account.entity.WxConfig;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AcService extends IService<Ac> {

    Page<Ac> getList(Page<Ac> page, QueryWrapper wrapper);

    BaseResult<Ac> getInfoByAcIp(String acIp);

    BaseResult<Ac> getInfoByAcName(String acName);

    BaseResult<Ac> getInfo4Wired();

    WxConfig getWxConfigById(Integer acId);

    DingTalkConfig getDingTalkConfigById(Integer acId);

}
