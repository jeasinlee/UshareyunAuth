package cn.ushare.account.entity;

public class Constant {

    /**
     * 认证方式，1账户密码，2短信，3微信，4一键，5员工授权，6二维码，7钉钉
     */
    public class AuthMethod {
        public final static int ACCOUNT_AUTH = 1;
        public final static int SMS_AUTH = 2;
        public final static int WX_AUTH = 3;
        public final static int ONEKEY_AUTH = 4;
        public final static int EMPLOYEE_AUTH = 5;
        public final static int QRCODE_AUTH = 6;
        public final static int DING_TALK_AUTH = 7;
        public final static int QIWEI_AUTH = 8;    //企微认证
        public final static int QUESTION_AUTH = 9;    //答题认证
        public final static int MAC_WHITE_LIST_AUTH = 99;// mac白名单自动登录
    }
    
}
