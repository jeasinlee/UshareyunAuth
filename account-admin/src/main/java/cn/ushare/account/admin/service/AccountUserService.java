package cn.ushare.account.admin.service;

import cn.ushare.account.dto.AccountUserForgetReq;
import cn.ushare.account.entity.AccountUser;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AccountUserService extends IService<AccountUser> {

    Page<AccountUser> getList(Page<AccountUser> page, QueryWrapper wrapper);

    Page<AccountUser> getLockedList(Page<AccountUser> page, QueryWrapper wrapper);

    Page<AccountUser> getDebtList(Page<AccountUser> page, QueryWrapper wrapper);

    BaseResult addOrUpdate(AccountUser accountUser);

    BaseResult login(String accountUserJson) throws Exception;

    AccountUser getDetail(String loginName, int isValid);

    BaseResult excelImport(MultipartFile file) throws Exception;

    void excelExport(String ids) throws Exception;

    void updateByIds(List<Integer> ids);

    BaseResult forgotPwd(AccountUserForgetReq forgetReq, AccountUser accountUser);
}
