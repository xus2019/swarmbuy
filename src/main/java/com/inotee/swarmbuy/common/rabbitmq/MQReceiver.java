package com.inotee.swarmbuy.common.rabbitmq;

import com.alibaba.fastjson.JSONObject;
import com.inotee.swarmbuy.entity.MiaoshaOrder;
import com.inotee.swarmbuy.entity.MiaoshaUser;
import com.inotee.swarmbuy.entity.vo.GoodsVo;
import com.inotee.swarmbuy.service.GoodsService;
import com.inotee.swarmbuy.service.MiaoshaService;
import com.inotee.swarmbuy.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Auther: xushuai56
 * @Date: 2020/9/7 19:58
 * @Description:
 */
@Slf4j
@Service
public class MQReceiver {

    @Resource
    GoodsService goodsService;

    @Resource
    OrderService orderService;

    @Resource
    MiaoshaService miaoshaService;

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receiveMiaosha(String message){
        MiaoshaMessage messages =  JSONObject.parseObject(message, MiaoshaMessage.class);
        MiaoshaUser user = messages.getUser();
        long goodsId = messages.getGoodsId();

        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock <= 0) {
            return;
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderInfoByUserIdAndGoodsId(user.getId(), goodsId);
        if(order != null) {
            return;
        }
        miaoshaService.miaosha(user, goods);
    }

    @RabbitListener(queues = MQConfig.QUEUE)
    public void receive(String message){
        log.info("-------------------receive message:"+message);

    }

    @RabbitListener(queues = MQConfig.FIRST_TOPIC_QUEUE)
    public void receiveFirstTopic(String message){
        log.info("-------------------receive message:"+message);
    }

    @RabbitListener(queues = MQConfig.SECOND_TOPIC_QUEUE)
    public void receiveSecondTopic(String message){
        log.info("-------------------receive message:"+message);
    }

    @RabbitListener(queues = MQConfig.HEADERS_QUEUE)
    public void receiveHeader(byte[] message){
        log.info("-------------------receive message:"+new String(message));
    }


}
