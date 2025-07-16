package cn.ushare.account.admin.service;

import cn.ushare.account.entity.QuestionBank;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author jixiang.li
 * @date 2021-09-01
 * @email jixiang.li@ushareyun.net
 */
public interface QuestionBankService extends IService<QuestionBank> {

    Page<QuestionBank> getList(Page<QuestionBank> page, QueryWrapper wrapper);

    List<QuestionBank> getAuthData(QueryWrapper wrapper);

    List<QuestionBank> getAuthData2(Integer count);
}
