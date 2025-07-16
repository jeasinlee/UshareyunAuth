package cn.ushare.account.admin.service;

import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.entity.BlackList;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface BlackListService extends IService<BlackList> {

    BaseResult excelImportPhone(MultipartFile file) throws Exception;

    BaseResult excelImportMac(MultipartFile file) throws Exception;

    void excelExportPhone(String ids) throws Exception;

    void excelExportMac(String ids) throws Exception;

    BaseResult add(BlackList blackList);

    BaseResult update(BlackList blackList);

    Page<BlackList> getList(Page<BlackList> page, QueryWrapper wrapper);

}
