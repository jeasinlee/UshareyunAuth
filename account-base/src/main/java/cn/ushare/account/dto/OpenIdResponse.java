package cn.ushare.account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OpenIdResponse {
    @JsonProperty("returnCode")
    private String returnCode;

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("returnMsg")
    private String returnMsg;

    @JsonProperty("data")
    private List<ZIpOpenId> data;
}
