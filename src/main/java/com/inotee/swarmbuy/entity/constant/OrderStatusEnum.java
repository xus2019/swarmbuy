package com.inotee.swarmbuy.entity.constant;

/**
 * 订单状态, 0新建未支付, 1已支付, 2已发货, 3已收货, 4已退款, 5已完成
 */
public enum OrderStatusEnum {
    NEW_ORDER_NO_PAY(0,"新建未支付"),
    ALREADY_PAY(1,"已支付"),
    ALREADY_CONSIGNMENT(2,"已发货"),
    RECEIVED_GOODS(3,"已收货"),
    REFUND(4,"已退款"),
    COMPLETED(5,"已完成")
    ;
    /**
     *枚举值
     * */
    private Integer value;
    /**
     * 状态描述
     * */
    private String desc;

    OrderStatusEnum(Integer value, String desc) {
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
