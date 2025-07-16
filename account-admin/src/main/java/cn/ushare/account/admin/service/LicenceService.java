package cn.ushare.account.admin.service;

import cn.ushare.account.dto.LicenceApplyParam;
import cn.ushare.account.dto.LicenceInfo;
import cn.ushare.account.entity.BaseResult;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface LicenceService {

    BaseResult<String> getApplyCode();

    BaseResult onlineLicence(LicenceApplyParam licenceApplyParam);

    BaseResult offlineLicence();

    BaseResult parseLicence(String fileName);

    BaseResult checkInfo();

    LicenceInfo getAccountInfo();

    BaseResult getSoftwareVersion();

    BaseResult checkActionConfig();
}
