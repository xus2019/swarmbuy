package com.inotee.swarmbuy.common.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Auther: xushuai56
 * @Date: 2020/9/7 19:56
 * @Description:
 */
@Slf4j
@Service
public class MQSender {

    @Resource
    AmqpTemplate amqpTemplate;


    /**
     * @Description: 发送秒杀消息
     */
    public void sendMiaoshaMessage(MiaoshaMessage message){
        String messages = JSONObject.toJSONString(message);
        log.info("-----sen message:" + messages);
        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE,messages);

    }

    public void sendMessage(Object message){
        log.info("-------------------send message:"+JSON.toJSONString(message));
        amqpTemplate.convertAndSend(message);
    }


    public void sendTopic(Object message) {
        log.info("-------------------send TOPIC message:"+JSON.toJSONString(message));
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,"topic.first",message+"1");
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,"topic.second",message+"2");
    }

    public void sendFanout(Object message){
        log.info("-------------------send TOPIC message:"+JSON.toJSONString(message));
        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE,"",message);
    }

    public void sendHeaders(Object message){

        log.info("-------------------send header message:"+message);

        MessageProperties properties = new MessageProperties();
        properties.setHeader("header1","value1");
        properties.setHeader("header2","value2");

        Message obj = new Message((message+"").getBytes(),properties);

        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE,"",obj);
    }
}
