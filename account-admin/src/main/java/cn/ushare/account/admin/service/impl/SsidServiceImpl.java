package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AuthBaseTemplateMapper;
import cn.ushare.account.admin.mapper.AuthTemplateMapper;
import cn.ushare.account.admin.mapper.SsidMapper;
import cn.ushare.account.admin.service.AcService;
import cn.ushare.account.admin.service.AuthRecordService;
import cn.ushare.account.admin.service.AuthUserService;
import cn.ushare.account.admin.service.SsidService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author jixiang.li
 * @since 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class SsidServiceImpl extends ServiceImpl<SsidMapper, Ssid> implements SsidService {

    @Autowired
    SsidMapper ssidMapper;
    @Autowired
    AuthRecordService authRecordService;
    @Autowired
    AuthUserService authUserService;
    @Autowired
    AcService acService;
    @Autowired
    SessionService sessionService;
    @Autowired
    AuthTemplateMapper authTemplateMapper;
    @Autowired
    AuthBaseTemplateMapper authBaseTemplateMapper;

    @Override
    public Page<Ssid> getList(Page<Ssid> page, QueryWrapper wrapper, Map<String, Object> map) {
        // 查询全部
        List<Ssid> list = ssidMapper.getList(page, wrapper);
        for (int i = 0; i < list.size(); i++) {
            Ssid item = list.get(i);
            // 控制器名称
            Ac ac = acService.getById(item.getAcId());
            if (ac != null) {
                item.setAcName(ac.getName());
            }

            // 用户数
            QueryWrapper<AuthUser> userNumQuery = new QueryWrapper();
            userNumQuery.eq("ssid", item.getName());
            userNumQuery.eq("is_valid", 1);
            //int userNum = authRecordService.count(userNumQuery);
            long userNum = authUserService.count(userNumQuery);
            item.setUserNum(userNum);

            // ap数
            /*
            QueryWrapper<AuthRecord> apNumQuery = new QueryWrapper();
            apNumQuery.eq("ssid", item.getName());
            apNumQuery.eq("is_valid", 1);
            apNumQuery.groupBy("ap_mac");
            int apNum = authRecordService.count(apNumQuery);
            item.setApNum(apNum);
            */
        }

        // 排序
        String orderName = (String) map.get("orderName");
        String orderRule = (String) map.get("orderRule");
        // ssid名称排序
        if (StringUtils.isNotBlank(orderName) && orderName.equals("name")) {
            if (StringUtils.isNotBlank(orderRule) && orderRule.equals("ascending")) {
                Collections.sort(list, new Comparator<Ssid>() {
                    @Override
                    public int compare(Ssid arg0, Ssid arg1) {
                        return arg0.getName().compareTo(arg1.getName());
                    }
                });
            } else {
                Collections.sort(list, new Comparator<Ssid>() {
                    @Override
                    public int compare(Ssid arg0, Ssid arg1) {
                        return arg1.getName().compareTo(arg0.getName());
                    }
                });
            }
        }
        // ac名称排序
        if (StringUtils.isNotBlank(orderName) && orderName.equals("acName")) {
            if (StringUtils.isNotBlank(orderRule) && orderRule.equals("ascending")) {
                Collections.sort(list, new Comparator<Ssid>() {
                    @Override
                    public int compare(Ssid arg0, Ssid arg1) {
                        return arg0.getAcName().compareTo(arg1.getAcName());
                    }
                });
            } else {
                Collections.sort(list, new Comparator<Ssid>() {
                    @Override
                    public int compare(Ssid arg0, Ssid arg1) {
                        return arg1.getAcName().compareTo(arg0.getAcName());
                    }
                });
            }
        }
        // 用户数排序
        if (StringUtils.isNotBlank(orderName) && orderName.equals("userNum")) {
            if (StringUtils.isNotBlank(orderRule) && orderRule.equals("ascending")) {
                Collections.sort(list, new Comparator<Ssid>() {
                    @Override
                    public int compare(Ssid arg0, Ssid arg1) {
                        return arg0.getUserNum().compareTo(arg1.getUserNum());
                    }
                });
            } else {
                Collections.sort(list, new Comparator<Ssid>() {
                    @Override
                    public int compare(Ssid arg0, Ssid arg1) {
                        return arg1.getUserNum().compareTo(arg0.getUserNum());
                    }
                });
            }
        }
        // 部门名称排序
        if (StringUtils.isNotBlank(orderName) && orderName.equals("departmentName")) {
            if (StringUtils.isNotBlank(orderRule) && orderRule.equals("ascending")) {
                Collections.sort(list, new Comparator<Ssid>() {
                    @Override
                    public int compare(Ssid arg0, Ssid arg1) {
                        return arg0.getDepartmentName().compareTo(arg1.getDepartmentName());
                    }
                });
            } else {
                Collections.sort(list, new Comparator<Ssid>() {
                    @Override
                    public int compare(Ssid arg0, Ssid arg1) {
                        return arg1.getDepartmentName().compareTo(arg0.getDepartmentName());
                    }
                });
            }
        }

        return page.setRecords(list);
    }

    @Override
    public void save(Integer acId, String name) {
        QueryWrapper<Ssid> queryWrapper = new QueryWrapper();
        queryWrapper.eq("ac_id", acId);
        queryWrapper.eq("name", name);
        queryWrapper.eq("is_valid", 1);
        List<Ssid> ssids = ssidMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(ssids)) {
            Ssid ssid = new Ssid();
            ssid.setAcId(acId);
            ssid.setName(name);
            Date date = new Date();
            ssid.setCreateTime(date);
            ssid.setUpdateTime(date);

            Ac ac = acService.getById(acId);
            if (ac != null) {
                ssid.setAcName(ac.getName());
                ssid.setAuthTemplateId(ac.getAuthTemplateId());
                ssid.setAuthMethod(ac.getAuthMethod());
            }

            ssidMapper.insert(ssid);
        }
    }

    @Override
    public void setDepartmentIdNull(Integer id) {
        ssidMapper.setDepartmentIdNull(id);
    }

    @Override
    public BaseResult add(Ssid ssid) {
        // 名称是否重复
        QueryWrapper<Ssid> queryWrapper = new QueryWrapper();
        queryWrapper.eq("ac_id", ssid.getAcId());
        queryWrapper.eq("name", ssid.getName());
        queryWrapper.eq("is_valid", 1);
        List<Ssid> querySsid = ssidMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(querySsid)) {
            return new BaseResult("0", "名称重复", null);
        }

        ssid.setIsValid(1);
        ssid.setUpdateTime(new Date());
        ssidMapper.insert(ssid);

        return new BaseResult();
    }

    @Override
    public BaseResult update(Ssid ssid) {
        // 名称是否重复
        QueryWrapper<Ssid> queryWrapper = new QueryWrapper();
        queryWrapper.eq("ac_id", ssid.getAcId());
        queryWrapper.eq("name", ssid.getName());
        queryWrapper.eq("is_valid", 1);
        Ssid querySsid = ssidMapper.selectOne(queryWrapper);
        if (querySsid != null && querySsid.getId() != ssid.getId()) {
            return new BaseResult("0", "名称重复", null);
        }

        ssidMapper.updateById(ssid);
        if (ssid.getDepartmentId() == null || ssid.getDepartmentId() == 0) {
            ssidMapper.setDepartmentIdNull(ssid.getId());
        }

        return new BaseResult(ssid);
    }

    @Override
    public BaseResult getInfoByName(Map<String, Object> map){
        Ssid ssid = ssidMapper.selectList(new QueryWrapper<Ssid>()
                .eq("name", map.get("name")).eq("ac_id", map.get("ac_id"))
                .orderByDesc("update_time")).get(0);

        // 查询模板
        AuthTemplate authTemplate = authTemplateMapper.selectById(
                ssid.getAuthTemplateId());
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
        ssid.setAuthTemplate(authTemplate);
        return new BaseResult(ssid);
    }
}
