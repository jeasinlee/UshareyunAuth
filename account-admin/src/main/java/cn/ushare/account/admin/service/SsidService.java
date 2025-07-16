package cn.ushare.account.admin.service;

import java.util.Map;
import cn.ushare.account.entity.Ssid;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface SsidService extends IService<Ssid> {

    BaseResult add(Ssid ssid);

    BaseResult update(Ssid ssid);

    void save(Integer acId, String name);

    void setDepartmentIdNull(Integer id);

    Page<Ssid> getList(Page<Ssid> page, QueryWrapper wrapper, Map<String, Object> map);

    BaseResult getInfoByName(Map<String, Object> map);
}
