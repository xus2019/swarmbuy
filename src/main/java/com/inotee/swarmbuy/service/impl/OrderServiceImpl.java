package com.inotee.swarmbuy.service.impl;

import com.inotee.swarmbuy.common.exception.GlobalException;
import com.inotee.swarmbuy.common.util.RedisUtil;
import com.inotee.swarmbuy.entity.MiaoshaOrder;
import com.inotee.swarmbuy.entity.MiaoshaUser;
import com.inotee.swarmbuy.entity.OrderInfo;
import com.inotee.swarmbuy.entity.constant.OrderStatusEnum;
import com.inotee.swarmbuy.entity.result.CodeMsg;
import com.inotee.swarmbuy.entity.vo.GoodsVo;
import com.inotee.swarmbuy.mapper.MiaoshaOrderMapper;
import com.inotee.swarmbuy.mapper.OrderInfoMapper;
import com.inotee.swarmbuy.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    MiaoshaOrderMapper miaoshaOrderMapper;

    @Resource
    OrderInfoMapper orderInfoMapper;

    @Resource
    RedisUtil redisUtil;

    /**
     * @param:
     * @return:
     * @Description: 优化：不查数据库，查缓存
     */
    @Override
    public MiaoshaOrder getMiaoshaOrderInfoByUserIdAndGoodsId(Long userId, long goodsId) {
        /*MiaoshaOrderExample example = new MiaoshaOrderExample();
        example.createCriteria()
                .andUserIdEqualTo(userId)
                .andGoodsIdEqualTo(goodsId);
        List<MiaoshaOrder> miaoshaOrders = miaoshaOrderMapper.selectByExample(example);
        if(!CollectionUtils.isEmpty(miaoshaOrders)){
            return miaoshaOrders.get(0);
        }*/
        MiaoshaOrder result = (MiaoshaOrder) redisUtil.get("miaoShaOrder:" + userId + ":" + goodsId);
        return result;
    }

    @Override
    public OrderInfo createMiaoshaOrder(MiaoshaUser user, GoodsVo goods) {
        //写入order_info
        OrderInfo orderInfo=this.insertOrderInfo(user,goods);

        //写入miaosha_order
        Long orderId = orderInfo.getId();
        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setGoodsId(goods.getId());
        miaoshaOrder.setOrderId(orderId);
        miaoshaOrder.setUserId(user.getId());
        miaoshaOrderMapper.insert(miaoshaOrder);

        redisUtil.set("miaoShaOrder:"+user.getId()+":"+goods.getId(),miaoshaOrder);

        return orderInfo;
    }

    @Override
    public OrderInfo getOrderById(long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        if(orderInfo==null){
            return null;
        }
        return orderInfo;
    }

    private OrderInfo insertOrderInfo(MiaoshaUser user, GoodsVo goods) {
        OrderInfo orderInfo = new OrderInfo();
        Date date = new Date();
        orderInfo.setCreateDate(date);
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(BigDecimal.valueOf(goods.getMiaoshaPrice()));
        orderInfo.setOrderChannel((byte) 1);
        orderInfo.setStatus(OrderStatusEnum.NEW_ORDER_NO_PAY.getValue().byteValue());
        orderInfo.setUserId(user.getId());
        orderInfoMapper.insertSelective(orderInfo); //自动填充id
        if(orderInfo.getId()==null){
            throw new GlobalException(CodeMsg.ORDER_INSERT_ERROR);
        }
        return orderInfo;
    }
}
