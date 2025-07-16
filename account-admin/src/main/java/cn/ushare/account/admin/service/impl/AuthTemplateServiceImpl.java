package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AdImageMapper;
import cn.ushare.account.admin.mapper.AuthTemplateMapper;
import cn.ushare.account.admin.service.AuthTemplateService;
import cn.ushare.account.entity.AdImage;
import cn.ushare.account.entity.AuthTemplate;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * @author jixiang.li
 * @date 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class AuthTemplateServiceImpl extends ServiceImpl<AuthTemplateMapper, AuthTemplate> implements AuthTemplateService {

    @Autowired
    AuthTemplateMapper authTemplateMapper;
    @Autowired
    AdImageMapper imageMapper;

    @Override
    public Page<AuthTemplate> getList(Page<AuthTemplate> page, QueryWrapper wrapper) {
        return page.setRecords(authTemplateMapper.getList(page, wrapper));
    }

    @Override
    public AuthTemplate getInfo(Integer id) {
        AuthTemplate authTemplate = authTemplateMapper.selectById(id);
        String idsStr = authTemplate.getBannerImageIds();
        if (idsStr != null && !idsStr.equals("")) {
            String[] idArray = idsStr.split(",");
            List<AdImage> imageList = imageMapper.selectBatchIds(Arrays.asList(idArray));
            authTemplate.setBannerImageList(imageList);
        }

        return authTemplate;
    }

}
