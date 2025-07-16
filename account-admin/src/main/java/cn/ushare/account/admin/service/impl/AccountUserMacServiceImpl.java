package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AccountUserMacMapper;
import cn.ushare.account.admin.mapper.AccountUserMapper;
import cn.ushare.account.admin.service.AccountUserMacService;
import cn.ushare.account.admin.service.WhiteListService;
import cn.ushare.account.entity.AccountUser;
import cn.ushare.account.entity.AccountUserMac;
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

import java.util.List;

/**
 * @author jixiang.li
 * @date 2022-04-12
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class AccountUserMacServiceImpl extends ServiceImpl<AccountUserMacMapper, AccountUserMac> implements AccountUserMacService {
    @Autowired
    AccountUserMacMapper userMacMapper;
    @Autowired
    AccountUserMapper userMapper;


    @Override
    public Page<AccountUserMac> getList(Page<AccountUserMac> page, QueryWrapper wrapper) {
        List<AccountUserMac> accountUserMacs = userMacMapper.getList(page, wrapper);

        return page.setRecords(accountUserMacs);
    }

    @Override
    public BaseResult add(AccountUserMac userMac) {
        AccountUser accountUser = userMapper.getDetail(userMac.getLoginName(), 1);
        if(null == accountUser){
            return new BaseResult("0", "账户错误或不存在", null);
        }

        if(null != accountUser.getBindMacNum() && accountUser.getBindMacNum()!= 0){
            QueryWrapper<AccountUserMac> queryWrapper = new QueryWrapper();
            queryWrapper.eq("login_name", userMac.getLoginName());
            int count = userMacMapper.selectCount(queryWrapper);
            if(count >= accountUser.getBindMacNum()){
                return new BaseResult("0", "账户名下绑定mac已超限，请删除其他mac", null);
            }
        }

        // 白名单是否重复
        QueryWrapper<AccountUserMac> macQueryWrapper = new QueryWrapper();
        macQueryWrapper.eq("login_name", userMac.getLoginName());
        macQueryWrapper.eq("mac", userMac.getMac());
        AccountUserMac accountUserMac = userMacMapper.selectOne(macQueryWrapper);
        if (accountUserMac != null) {
            return new BaseResult("0", "已经加入白名单，请勿重复添加", null);
        }
        userMac.setUserId(accountUser.getId());

        userMacMapper.insert(userMac);
        return new BaseResult();
    }

    @Override
    public BaseResult update(AccountUserMac userMac) {
        if(null == userMac.getId()){
            return new BaseResult("0", "ID不能为空", null);
        }
        AccountUser accountUser = userMapper.getDetail(userMac.getLoginName(), 1);
        if(null == accountUser){
            return new BaseResult("0", "账户错误或不存在", null);
        }
        // 白名单是否重复
        QueryWrapper<AccountUserMac> macQueryWrapper = new QueryWrapper();
        macQueryWrapper.eq("login_name", userMac.getLoginName());
        macQueryWrapper.eq("mac", userMac.getMac());
        AccountUserMac accountUserMac = userMacMapper.selectOne(macQueryWrapper);
        if (accountUserMac != null) {
            if(!accountUserMac.getId().equals(userMac.getId())) {
                return new BaseResult("0", "信息重复", null);
            }
        }
        userMac.setUserId(accountUser.getId());
        userMacMapper.updateById(userMac);

        return new BaseResult();
    }

}
