package cn.ushare.account.dto;

import lombok.Data;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
@Data
public class CompanyInfoReq {

    private static final long serialVersionUID = 1L;

    private String logo;

    private String companyName;

    private String redirectUrl;

    private String redirectUrlValid;

    private String showCode;

    private String official;

    private String smsCheck;

}
