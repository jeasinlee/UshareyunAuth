package cn.ushare.account.admin.service;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface HostUrlService {

    String getFrontUrl(HttpServletRequest request);

    String getServerUrl(HttpServletRequest request);

}
