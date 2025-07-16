package cn.ushare.account.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
public class LicenceInfo {

    private static final long serialVersionUID = 1L;

    private Integer acAmount;

    private Integer staAmount;

    private Integer authWx;
    private Integer authQuestion;
    private Integer authAction;
    private Integer authDingtalk;
    private Integer authQiwei;

    private String cpuSerial;

    private String mainBoardSerial;

    private String osSerial;

    private String account;

    private Integer isAccount;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;
}
