package cn.ushare.account.admin.service;

import cn.ushare.account.dto.AdminChangePasswordReq;
import cn.ushare.account.dto.AdminForgetPasswordReq;
import cn.ushare.account.dto.AdminLoginReq;
import cn.ushare.account.dto.AdminSendSmsReq;
import cn.ushare.account.entity.Administrator;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AdministratorService extends IService<Administrator> {

    BaseResult login(AdminLoginReq adminLogin);

    BaseResult logout();

    BaseResult sendSmsCode(AdminSendSmsReq param) throws Exception;

    BaseResult changePassword(AdminChangePasswordReq param);

    BaseResult forgetPassword(AdminForgetPasswordReq param);

    Page<Administrator> getList(Page<Administrator> page, QueryWrapper wrapper);

}
