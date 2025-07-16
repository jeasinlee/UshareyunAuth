package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.*;
import cn.ushare.account.admin.service.AcService;
import cn.ushare.account.entity.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jixiang.li
 * @date 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class AcServiceImpl extends ServiceImpl<AcMapper, Ac> implements AcService {

    @Autowired
    AcMapper acMapper;
    @Autowired
    DeviceBrandMapper deviceBrandMapper;
    @Autowired
    AuthTemplateMapper authTemplateMapper;
    @Autowired
    AuthBaseTemplateMapper authBaseTemplateMapper;
    @Autowired
    WxConfigMapper wxConfigMapper;
    @Autowired
    DingTalkConfigMapper dingTalkConfigMapper;

    @Override
    public Page<Ac> getList(Page<Ac> page, QueryWrapper wrapper) {
        return page.setRecords(acMapper.getList(page, wrapper));
    }

    @Override
    public BaseResult<Ac> getInfoByAcIp(String acIp) {
        // 查询ac
        QueryWrapper<Ac> acQuery = new QueryWrapper();
        acQuery.and( wrapper -> wrapper.eq("ip", acIp).or().like("nas_ip", acIp));
        acQuery.eq("is_valid", 1);
        Ac ac = acMapper.selectOne(acQuery);
        if (ac == null) {
            return new BaseResult("0", "没有IP " + acIp + " 对应的AC控制器", null);
        }

        // 查询品牌
        QueryWrapper<DeviceBrand> brandQuery = new QueryWrapper();
        brandQuery.eq("id", ac.getBrandId());
        brandQuery.eq("is_valid", 1);
        DeviceBrand brand = deviceBrandMapper.selectOne(brandQuery);
        ac.setBrand(brand);

        // 查询模板
        AuthTemplate authTemplate = authTemplateMapper.selectById(
                ac.getAuthTemplateId());
        if (authTemplate == null) {
            return new BaseResult("0", "没有设置认证模板", null);
        }

        // 查询基础模板
        AuthBaseTemplate authBaseTemplate = authBaseTemplateMapper
                .selectById(authTemplate.getBaseTemplateId());
        if (authBaseTemplate == null) {
            return new BaseResult("0", "没有设置认证基础模板", null);
        }

        authTemplate.setBaseTemplate(authBaseTemplate);
        ac.setAuthTemplate(authTemplate);
        return new BaseResult(ac);
    }

    @Override
    public BaseResult<Ac> getInfoByAcName(String acName) {
        // 查询ac
        QueryWrapper<Ac> acQuery = new QueryWrapper();
        acQuery.eq("name", acName);
        acQuery.eq("is_valid", 1);
        Ac ac = acMapper.selectOne(acQuery);
        if (ac == null) {
            return new BaseResult("0", "没有ac名称 " + acName + " 对应的AC控制器", null);
        }

        // 查询品牌
        QueryWrapper<DeviceBrand> brandQuery = new QueryWrapper();
        brandQuery.eq("id", ac.getBrandId());
        brandQuery.eq("is_valid", 1);
        DeviceBrand brand = deviceBrandMapper.selectOne(brandQuery);
        ac.setBrand(brand);

        // 查询模板
        AuthTemplate authTemplate = authTemplateMapper.selectById(
                ac.getAuthTemplateId());
        if (authTemplate == null) {
            return new BaseResult("0", "没有设置认证模板", null);
        }

        // 查询基础模板
        AuthBaseTemplate authBaseTemplate = authBaseTemplateMapper
                .selectById(authTemplate.getBaseTemplateId());
        if (authBaseTemplate == null) {
            return new BaseResult("0", "没有设置认证基础模板", null);
        }

        authTemplate.setBaseTemplate(authBaseTemplate);
        ac.setAuthTemplate(authTemplate);
        return new BaseResult(ac);
    }

    @Override
    public BaseResult<Ac> getInfo4Wired() {
        // 查询ac
        QueryWrapper<Ac> acQuery = new QueryWrapper();
        acQuery.eq("is_wired", 1);
        acQuery.orderByDesc("update_time");
        Ac ac = null;
        List<Ac> acList = acMapper.selectList(acQuery);
        if(CollectionUtils.isNotEmpty(acList)){
            ac = acList.get(0);
        }

        if (ac == null) {
            return new BaseResult("0", "系统不支持有线认证", null);
        }

        // 查询品牌
        QueryWrapper<DeviceBrand> brandQuery = new QueryWrapper();
        brandQuery.eq("id", ac.getBrandId());
        brandQuery.eq("is_valid", 1);
        DeviceBrand brand = deviceBrandMapper.selectOne(brandQuery);
        ac.setBrand(brand);

        // 查询模板
        AuthTemplate authTemplate = authTemplateMapper.selectById(
                ac.getAuthTemplateId());
        if (authTemplate == null) {
            return new BaseResult("0", "没有设置认证模板", null);
        }

        // 查询基础模板
        AuthBaseTemplate authBaseTemplate = authBaseTemplateMapper
                .selectById(authTemplate.getBaseTemplateId());
        if (authBaseTemplate == null) {
            return new BaseResult("0", "没有设置认证基础模板", null);
        }

        authTemplate.setBaseTemplate(authBaseTemplate);
        ac.setAuthTemplate(authTemplate);
        return new BaseResult(ac);
    }

    @Override
    public WxConfig getWxConfigById(Integer acId) {
        Ac ac = acMapper.selectById(acId);
        WxConfig wxConfig = wxConfigMapper.selectById(ac.getWxShopConfigId());
        return wxConfig;
    }

    @Override
    public DingTalkConfig getDingTalkConfigById(Integer acId) {
        Ac ac = acMapper.selectById(acId);
        DingTalkConfig dingTalkConfig = dingTalkConfigMapper.selectById(ac.getDingTalkConfigId());
        return dingTalkConfig;
    }

}
