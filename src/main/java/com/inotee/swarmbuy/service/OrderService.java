package com.inotee.swarmbuy.service;

import com.inotee.swarmbuy.entity.MiaoshaOrder;
import com.inotee.swarmbuy.entity.MiaoshaUser;
import com.inotee.swarmbuy.entity.OrderInfo;
import com.inotee.swarmbuy.entity.vo.GoodsVo;

public interface OrderService {
    MiaoshaOrder getMiaoshaOrderInfoByUserIdAndGoodsId(Long userId, long goodsId);

    OrderInfo createMiaoshaOrder(MiaoshaUser user, GoodsVo goods);

    OrderInfo getOrderById(long orderId);
}
