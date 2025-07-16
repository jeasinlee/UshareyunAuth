package cn.ushare.account.admin.service;

import java.util.Map;
import cn.ushare.account.entity.SystemConfig;
import cn.ushare.account.dto.SystemTimeSyncReq;
import cn.ushare.account.entity.BaseResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface SystemCmdService {

    void syncTime(String time);

    void rebootSystem();

    BaseResult rebootTomcat();

    BaseResult exportDb(String savePath, String fileName);

    BaseResult importDb(String filePath, String fileName);

    BaseResult getSystemInfo();

    String getOsSerial();

    Map<String, String> getHardwareSn();

    BaseResult getSystemStatus();

}
