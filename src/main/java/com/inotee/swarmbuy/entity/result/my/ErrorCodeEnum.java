package com.inotee.swarmbuy.entity.result.my;

public enum ErrorCodeEnum {

    /**成功！*/
    RETURN_SUCCEE("1", "return.success"),
    /**操作失败*/
    RETURN_FAIL("2", "return.fail"),
    /**操作异常*/
    RETURN_ERROR("3", "return.error"),
    /**未知错误*/
    UNKNOWN_ERROR("4", "unknow.error"),


    /**用户不存在*/
    PASSWORD_ERRPOR("5", "密码错误"),
    /**用户不存在*/
    USER_UNKNOW("6", "用户不存在");





    private String code;

    private String msgKey;

    private ErrorCodeEnum(String code, String msgKey) {
        this.code = code;
        this.msgKey = msgKey;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsgKey() {
        return msgKey;
    }

    public void setMsgKey(String msgKey) {
        this.msgKey = msgKey;
    }


    public static boolean isExistCode(String code) {
        if (code == null) {
            return false;
        }
        ErrorCodeEnum[] values = ErrorCodeEnum.values();
        for (ErrorCodeEnum item : values) {
            if (item.code.equals(code)) {
                return true;
            }
        }
        return false;

    }

    public static String getCodeByMsgKey(String msgKey) {
        if (msgKey == null) {
            return "";
        }
        ErrorCodeEnum[] values = ErrorCodeEnum.values();
        for (ErrorCodeEnum item : values) {
            if (item.msgKey.equals(msgKey)) {
                return item.getCode();
            }
        }
        return msgKey;

    }
}
