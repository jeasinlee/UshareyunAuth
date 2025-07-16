package cn.ushare.account.admin.service;

import cn.ushare.account.entity.BaseResult;

/**
 * @author jixiang.li
 * @date 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
public interface SmsSendService {

    BaseResult send(String phone, String text, Integer businessType) throws Exception;

    BaseResult sendAlarm(String phone) throws Exception;

    BaseResult sendByWebService(String phone, String text);
}
