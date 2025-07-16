package cn.ushare.account.admin.service.impl;

import cn.ushare.account.admin.mapper.AcMapper;
import cn.ushare.account.admin.mapper.AuthQrcodeMapper;
import cn.ushare.account.admin.service.AuthQrcodeService;
import cn.ushare.account.admin.service.HostUrlService;
import cn.ushare.account.admin.session.SessionService;
import cn.ushare.account.entity.Ac;
import cn.ushare.account.entity.AuthQrcode;
import cn.ushare.account.entity.BaseResult;
import cn.ushare.account.util.DateUtil;
import cn.ushare.account.util.QRCodeUtil;
import cn.ushare.account.util.StringUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * @author jixiang.li
 * @date 2019-03-26
 * @email jixiang.li@ushareyun.net
 */
@Service
@Transactional
@Slf4j
public class AuthQrcodeServiceImpl extends ServiceImpl<AuthQrcodeMapper, AuthQrcode> implements AuthQrcodeService {

    @Autowired
    HttpServletRequest request;
    @Autowired
    AuthQrcodeMapper authQrcodeMapper;
    @Autowired
    SessionService sessionService;
    @Autowired
    HostUrlService hostUrlService;
    @Autowired
    AcMapper acMapper;

    @Value("${path.uploadPath}")
    String uploadPath;// 文件上传路径

    @Override
    public Page<AuthQrcode> getList(Page<AuthQrcode> page, QueryWrapper wrapper) {
        List<AuthQrcode> list = authQrcodeMapper.getList(page, wrapper);

        for (int i = 0; i < list.size(); i++) {
            AuthQrcode item = list.get(i);
            item.setIsDefaultChecked(item.getIsDefault() == 1 ? true : false);
        }

        return page.setRecords(authQrcodeMapper.getList(page, wrapper));
    }

    @Override
    public AuthQrcode getValidCode(String sn) {
        return authQrcodeMapper.getValidCode(sn);
    }

    @Override
    public BaseResult add(AuthQrcode authQrcode) {
        // 生成随机编码
        String sn = DateUtil.date2Str(new Date(), "YYYYMMddHH");
        sn += StringUtil.getRandomString(4);

        // 查询是否重复
        QueryWrapper<AuthQrcode> repeatQuery = new QueryWrapper();
        repeatQuery.eq("sn", sn);
        repeatQuery.eq("is_valid", 1);
        AuthQrcode repeatOne = authQrcodeMapper.selectOne(repeatQuery);
        if (repeatOne != null) {
            return new BaseResult("0", "编码重复，请重新生成", null);
        }

        // 二维码内容Url
        String contentUrl = hostUrlService.getServerUrl(request);
        Ac ac = acMapper.getInfo(authQrcode.getAcId());
        if (ac.getBrandCode().contains("ruckus")) {
            contentUrl += "/ruckus";
        } else if(ac.getBrandCode().contains("huawei")){
            contentUrl += "/huawei";
        }else if(ac.getBrandCode().contains("ruijie")){
            contentUrl += "/ruijie";
        }else if(ac.getBrandCode().contains("cisco")){
            contentUrl += "/cisco";
        }else if(ac.getBrandCode().contains("h3c")){
            contentUrl += "/h3c";
        }else if(ac.getBrandCode().contains("aruba")){
            contentUrl += "/aruba";
        }else if(ac.getBrandCode().contains("wired")){
            contentUrl += "/wired";
        }else if(ac.getBrandCode().contains("tplink")){
            contentUrl += "/tplink";
        }
        contentUrl += "/qrcodeLogin?qrcodeSn=" + sn;

        // 生成二维码图片
        String savePath = uploadPath + "/qrcode";// 二维码图片存储路径
        String logoPath = null;// 二维码中间显示的logo
        QRCodeUtil.zxingCodeCreate(contentUrl, savePath, sn, 250, logoPath);

        // 如果设为默认，则其他二维码取消默认
        if (authQrcode.getIsDefault() == 1) {
            QueryWrapper<AuthQrcode> query = new QueryWrapper();
            query.eq("ac_id", authQrcode.getAcId());
            query.eq("is_valid", 1);
            List<AuthQrcode> list = authQrcodeMapper.selectList(query);
            // 同一个ac下，选中的这个置1，其他置0
            for (int i = 0; i < list.size(); i++) {
                AuthQrcode item = list.get(i);
                item.setIsDefault(0);
                authQrcodeMapper.updateById(item);
            }
        }

        authQrcode.setSn(sn);
        authQrcode.setUrl(contentUrl);
        authQrcode.setImageUrl("/qrcode/" + sn + ".jpg");
        authQrcode.setIsValid(1);
        authQrcodeMapper.insert(authQrcode);

        return new BaseResult(authQrcode);
    }

    @Override
    public BaseResult delete(Integer id) {
        AuthQrcode authQrcode = authQrcodeMapper.selectById(id);
        // 删除图片
        String imagePath = uploadPath + "/qrcode/" + authQrcode.getSn() + ".jpg";
        File file = new File(imagePath);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        return new BaseResult();
    }

}
