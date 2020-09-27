package com.inotee.swarmbuy.entity.constant;

public enum MiaoShaStatusEnum {

    NOT_BEGIN(0,"秒杀未开始"),
    BEGIN_ING(0,"秒杀进行中"),
    ALREADY_END(0,"秒杀已结束")
    ;
    /**
     *枚举值
     * */
    private Integer value;
    /**
     * 状态描述
     * */
    private String desc;

    MiaoShaStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
