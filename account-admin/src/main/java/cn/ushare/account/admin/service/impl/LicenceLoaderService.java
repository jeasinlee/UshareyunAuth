package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.service.LicenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cn.ushare.account.admin.config.ApplicationContextProvider;
import cn.ushare.account.entity.Ap;
import cn.ushare.account.entity.BaseResult;
import lombok.extern.slf4j.Slf4j;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Slf4j
public class LicenceLoaderService implements Runnable {

    LicenceService licenceService;

    public LicenceLoaderService() {
        this.licenceService = ApplicationContextProvider.getBean(LicenceService.class);
    }

    @Override
    public void run() {
        licenceService.parseLicence("licence.ushare");
    }

}
