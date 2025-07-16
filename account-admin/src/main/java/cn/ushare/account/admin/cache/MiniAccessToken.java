package cn.ushare.account.admin.cache;

import lombok.Data;

import java.util.Date;

@Data
public class MiniAccessToken {
    String accessToken;
    Date expireTime;
}
