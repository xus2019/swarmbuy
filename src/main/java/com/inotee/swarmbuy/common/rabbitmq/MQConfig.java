package com.inotee.swarmbuy.common.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther: xushuai56
 * @Date: 2020/9/7 19:54
 * @Description:
 */
@Configuration
public class MQConfig {
    public static final String QUEUE="queue";
    public static final String MIAOSHA_QUEUE="miaosha.queue";


    @Bean
    public Queue miaoshaQueue(){
        return new Queue(MIAOSHA_QUEUE,true);
    }

    @Bean
    public Queue queue(){
        return new Queue(QUEUE,true);
    }


    //--------------------topic-----------------------

    public static final String FIRST_TOPIC_QUEUE="topic.firstQueue";
    public static final String SECOND_TOPIC_QUEUE="topic.secondQueue";
    public static final String TOPIC_EXCHANGE="topicExchange";

    //创建两个queue
    @Bean
    public Queue topicFirstQueue(){
        return new Queue(FIRST_TOPIC_QUEUE,true);
    }

    @Bean
    public Queue topicSecondQueue(){
        return new Queue(SECOND_TOPIC_QUEUE,true);
    }

    //创建一个exchange
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(TOPIC_EXCHANGE);
    }
    //将两个queue与exchange关联起来

    @Bean
    public Binding firstTopicBinging(){
        return BindingBuilder
                .bind(topicFirstQueue())
                .to(topicExchange())
                .with("topic.first");
    }

    @Bean
    public Binding secondTopicBinging(){
        return BindingBuilder
                .bind(topicSecondQueue())
                .to(topicExchange())
                .with("topic.#");
    }


    //--------------------Fanout模式 -----------------------

    public static final String FANOUT_EXCHANGE = "fanoutxchage";

    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    @Bean
    public Binding fanoutBinding1(){
        return BindingBuilder.bind(topicFirstQueue()).to(fanoutExchange());
    }

    @Bean
    public Binding fanoutBinding2(){
        return BindingBuilder.bind(topicSecondQueue()).to(fanoutExchange());
    }


    //--------------------Header模式 -----------------------

    public static final String HEADERS_QUEUE = "headers.queue";
    public static final String HEADERS_EXCHANGE = "headersexchange";

    @Bean
    public HeadersExchange headersExchange(){
        return new HeadersExchange(HEADERS_EXCHANGE);
    }
    @Bean
    public Queue headersQueue(){
        return new Queue(HEADERS_QUEUE,true);
    }

    @Bean
    public Binding headerBuinding(){

        Map<String,Object> map = new HashMap<String,Object>();
        map.put("header1","value1");
        map.put("header2","value2");
        return BindingBuilder
                .bind(headersQueue())
                .to(headersExchange())
                .whereAll(map)
                .match();
    }
}
