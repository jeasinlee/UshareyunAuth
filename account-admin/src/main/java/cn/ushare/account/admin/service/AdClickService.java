package cn.ushare.account.admin.service;

import java.util.List;
import java.util.Map;
import cn.ushare.account.entity.AdClick;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AdClickService extends IService<AdClick> {

    List<Map<String, Integer>> statisticToday();

    List<Map<String, Integer>> statisticInDays(Integer days);

    Page<AdClick> getList(Page<AdClick> page, QueryWrapper wrapper);

}
