package com.inotee.swarmbuy.controller;

import com.inotee.swarmbuy.common.rabbitmq.MQReceiver;
import com.inotee.swarmbuy.common.rabbitmq.MQSender;
import com.inotee.swarmbuy.entity.result.Result;
import com.inotee.swarmbuy.entity.result.my.BaseService;
import com.inotee.swarmbuy.entity.result.my.ErrorCodeEnum;
import com.inotee.swarmbuy.entity.result.my.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class HelloController extends BaseService {

    @Resource
    MQSender sender;

    @Resource
    MQReceiver receiver;

    @RequestMapping("/hello")
    @ResponseBody
    public String HelloSwarmBuy(){
        return "Hello-Swarm-56416546";
    }

    @RequestMapping("/thymeleaf")
    public String testThymeleaf(Model model){
        model.addAttribute("name","xushuai");
        return "hello";
    }

    @RequestMapping("/response")
    @ResponseBody
    public ResponseEntity responseTest(){
        return buildSuccess("hello response entity");
    }

    @RequestMapping("/response-error")
    @ResponseBody
    public ResponseEntity responseErrorTest(){

        return buildError(ErrorCodeEnum.UNKNOWN_ERROR);
    }
    @RequestMapping("/t1")
    public String thyme1(Model model){

        model.addAttribute("title","title111").addAttribute("info","infooooooo");
        return "t1";
    }

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> testMq(){
        sender.sendMessage("hello rabbitMQ");
        return Result.success("Hello，world");
    }

    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> topicMQ(){
        sender.sendTopic("hello topic queue");
        return Result.success("Hello，topic");
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> fanoutMQ(){
        sender.sendFanout("hello fanout");
        return Result.success("Hello，fanout");
    }

    @RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> headerMQ(){
        sender.sendHeaders("hello headers");
        return Result.success("Hello，header");
    }


}
