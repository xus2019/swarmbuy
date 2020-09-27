package com.inotee.swarmbuy.entity.vo;

import com.inotee.swarmbuy.entity.OrderInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Auther: xushuai56
 * @Date: 2020/9/6 16:11
 * @Description:
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVo {
    private GoodsVo goods;
    private OrderInfo order;
}
