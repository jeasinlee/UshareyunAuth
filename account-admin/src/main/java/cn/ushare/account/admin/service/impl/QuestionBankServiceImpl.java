package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.QuestionBankMapper;
import cn.ushare.account.admin.service.QuestionBankService;
import cn.ushare.account.entity.QuestionBank;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author jixiang.li
 * @since 2021-09-01
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class QuestionBankServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank> implements QuestionBankService {

    @Autowired
    QuestionBankMapper questionBankMapper;

    @Override
    public Page<QuestionBank> getList(Page<QuestionBank> page, QueryWrapper wrapper) {
        return page.setRecords(questionBankMapper.getList(page, wrapper));
    }

    @Override
    public List<QuestionBank> getAuthData(QueryWrapper wrapper) {
        return questionBankMapper.getAuthData(wrapper);
    }

    @Override
    public List<QuestionBank> getAuthData2(Integer count) {
        return questionBankMapper.getAuthData2(count);
    }

}
