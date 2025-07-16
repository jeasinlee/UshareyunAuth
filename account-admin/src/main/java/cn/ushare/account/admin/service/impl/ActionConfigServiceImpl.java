package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.service.ActionConfigService;
import cn.ushare.account.admin.mapper.ActionConfigMapper;
import cn.ushare.account.entity.ActionConfig;
import cn.ushare.account.entity.BasePage;
import cn.ushare.account.entity.BasePageResult;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * @author jixiang.li
 * @since 2020-01-14
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class ActionConfigServiceImpl extends ServiceImpl<ActionConfigMapper, ActionConfig> implements ActionConfigService {

    @Autowired
    ActionConfigMapper actionConfigMapper;

    @Override
    public Page<ActionConfig> getList(Page<ActionConfig> page, QueryWrapper wrapper) {
        return page.setRecords(actionConfigMapper.getList(page, wrapper));
    }

    @Override
    public BaseResult update(ActionConfig actionConfig) {
        //更新默认值
        int result = actionConfigMapper.clearDefault();
        log.info("====update actionConfig" + result);
        actionConfig.setIsCur(1);
        if(null != actionConfig) {
            actionConfigMapper.updateById(actionConfig);
        } else {
            actionConfigMapper.insert(actionConfig);
        }

        return new BaseResult(actionConfig);
    }

}
