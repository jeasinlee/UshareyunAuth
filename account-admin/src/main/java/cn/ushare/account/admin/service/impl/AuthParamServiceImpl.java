package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AuthParamMapper;
import cn.ushare.account.admin.service.AuthParamService;
import cn.ushare.account.entity.AuthParam;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * auth_param表用于保存用户登录时的全部参数，
 * 1. 表中ip字段不会重复，每次新增，如果存在该ip的记录，则update，
 *    mac字段会重复，因为一个mac可以使用不同ip登录
 */
@Service
@Transactional
@Slf4j
public class AuthParamServiceImpl extends ServiceImpl<AuthParamMapper, AuthParam> implements AuthParamService {
    
    @Autowired
    AuthParamMapper authParamMapper;
    
    @Override
    public Page<AuthParam> getList(Page<AuthParam> page, QueryWrapper wrapper) {
        return page.setRecords(authParamMapper.getList(page, wrapper));
    }

    /**
     * 新增或更新
     */
    @Override
    public BaseResult addOrUpdateByIp(AuthParam authParam) throws Exception {
        if (authParam.getUserIp() == null || authParam.getUserIp().equals("")) {
            throw new Exception("userIp不能为空");
        }
        QueryWrapper<AuthParam> query = new QueryWrapper();
        query.eq("user_ip", authParam.getUserIp());
        if(StringUtils.isNotBlank(authParam.getSsid())) {
            query.eq("ssid", authParam.getSsid());
        }
        query.orderByDesc("update_time");
        List<AuthParam> list = authParamMapper.selectList(query);
        if (CollectionUtils.isEmpty(list)) {
            // 注意要加更新时间，在ip或mac重复时，用更新时间判断谁是当前用户
            authParam.setUpdateTime(new Date());
            authParamMapper.insertParam(authParam);
        } else {
            authParam.setId(list.get(0).getId());
            authParamMapper.updateById(authParam);
        }
        return new BaseResult(authParam.getId());
    }

    /**
     * 新增或更新
     */
    @Override
    public BaseResult addOrUpdateByMac(AuthParam authParam) throws Exception {
        if (authParam.getUserMac() == null || authParam.getUserMac().equals("")) {
            throw new Exception("userMac不能为空");
        }
        QueryWrapper<AuthParam> query = new QueryWrapper();
        query.eq("user_mac", authParam.getUserMac());
        query.orderByDesc("update_time");
        List<AuthParam> list = authParamMapper.selectList(query);
        if (CollectionUtils.isEmpty(list)) {
            // 注意要加更新时间，在ip或mac重复时，用更新时间判断谁是当前用户
            authParam.setUpdateTime(new Date());
            authParamMapper.insertParam(authParam);
        } else {
            authParam.setId(list.get(0).getId());
            authParamMapper.updateById(authParam);
        }
        return new BaseResult(authParam.getId());
    }

    /**
     * 根据ip查询最新记录
     */
    @Override
    public AuthParam getByUserIp(String ip) {
        QueryWrapper<AuthParam> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_ip", ip);
        queryWrapper.orderByDesc("update_time");
        List<AuthParam> list = authParamMapper.selectList(queryWrapper);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    /**
     * 根据mac查询最新记录
     */
    @Override
    public AuthParam getByUserMac(String mac) {
        QueryWrapper<AuthParam> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_mac", mac);
        queryWrapper.orderByDesc("update_time");
        List<AuthParam> list = authParamMapper.selectList(queryWrapper);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public AuthParam getByUserMacAndAcip(String mac, String acIp) {
        QueryWrapper<AuthParam> queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_mac", mac);
        queryWrapper.eq("ac_ip", acIp);
        queryWrapper.orderByDesc("update_time");

        List<AuthParam> list = authParamMapper.selectList(queryWrapper);
        if (list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

}
