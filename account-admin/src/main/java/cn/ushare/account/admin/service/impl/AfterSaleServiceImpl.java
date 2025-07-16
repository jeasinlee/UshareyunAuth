package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AfterSaleMapper;
import cn.ushare.account.admin.service.AfterSaleService;
import cn.ushare.account.entity.AfterSale;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jixiang.li
 * @date 2019-05-02
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class AfterSaleServiceImpl extends ServiceImpl<AfterSaleMapper, AfterSale> implements AfterSaleService {

    @Autowired
    AfterSaleMapper afterSaleMapper;

    @Override
    public Page<AfterSale> getList(Page<AfterSale> page, QueryWrapper wrapper) {
        return page.setRecords(afterSaleMapper.getList(page, wrapper));
    }

}
