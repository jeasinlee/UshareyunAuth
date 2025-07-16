package cn.ushare.account.admin.service;

import org.springframework.web.multipart.MultipartFile;

import cn.ushare.account.entity.Ap;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface FileUploadService {

    BaseResult upload(MultipartFile file, String savePath,
            String fileName);

}
