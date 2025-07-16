package cn.ushare.account.admin.service;

import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.FuncResource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface FuncResourceService extends IService<FuncResource> {

    List<String> getUriListByUserId(int id);

    BaseResult getTree(boolean isAccount);

    BaseResult getMenuPathList(boolean isAccount);

    Page<FuncResource> getList(Page<FuncResource> page, QueryWrapper wrapper);

}
