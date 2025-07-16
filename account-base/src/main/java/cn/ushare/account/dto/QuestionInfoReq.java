package cn.ushare.account.dto;

import lombok.Data;

@Data
public class QuestionInfoReq {

    private String questionAuth;

    private String questionStyle;

    private String questionCount;

    private String questionThreshold;

    private String questionCollection;
}
