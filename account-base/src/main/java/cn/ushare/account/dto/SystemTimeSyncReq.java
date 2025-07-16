package cn.ushare.account.dto;

import lombok.Data;

/**
 * @author jixiang.li
 * @since 2019-03-15
 * @email jixiang.li@ushareyun.net
 */
/**
 * 系统时间同步
 */
@Data
public class SystemTimeSyncReq {

    private static final long serialVersionUID = 1L;

    private String url;

    private String datetime;

    private String sync;// 1自动同步，2手动同步

}
