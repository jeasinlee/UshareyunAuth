package cn.ushare.account.admin.service;

import cn.ushare.account.entity.AuthQrcode;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface AuthQrcodeService extends IService<AuthQrcode> {

    AuthQrcode getValidCode(String sn);

    Page<AuthQrcode> getList(Page<AuthQrcode> page, QueryWrapper wrapper);

    BaseResult add(AuthQrcode authQrcode);

    BaseResult delete(Integer id);

}
