package com.inotee.swarmbuy.controller;

import com.inotee.swarmbuy.common.rabbitmq.MQSender;
import com.inotee.swarmbuy.common.rabbitmq.MiaoshaMessage;
import com.inotee.swarmbuy.common.util.RedisUtil;
import com.inotee.swarmbuy.entity.MiaoshaOrder;
import com.inotee.swarmbuy.entity.MiaoshaUser;
import com.inotee.swarmbuy.entity.result.CodeMsg;
import com.inotee.swarmbuy.entity.result.Result;
import com.inotee.swarmbuy.entity.vo.GoodsVo;
import com.inotee.swarmbuy.service.GoodsService;
import com.inotee.swarmbuy.service.MiaoshaService;
import com.inotee.swarmbuy.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

    @Resource
    GoodsService goodsService;

    @Resource
    OrderService orderService;

    @Resource
    MiaoshaService miaoshaService;

    @Resource
    RedisUtil redisUtil;

    @Resource
    MQSender mqSender;

    private HashMap<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

    //库存预热
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.queryGoodsList();
        if (goodsList == null) {
            return;
        }
        goodsList.forEach(goods -> {
            redisUtil.set("preStock" + goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);
        });
    }


    @RequestMapping(value = "/{path}/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> list(Model model, MiaoshaUser user,
                                @RequestParam("goodsId") long goodsId,
                                @PathVariable("path") String path) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //验证path
        boolean check = miaoshaService.checkPath(user, goodsId, path);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        //内存标记
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //预减库存，减到0之后过来的请求还是会判断redis，所以要加上内存标记
        long stock = redisUtil.decr("preStock" + goodsId, 1);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);

        }

        //判断是否已经秒杀到 ,使用唯一索引解决重复秒杀问题
        MiaoshaOrder order = orderService.getMiaoshaOrderInfoByUserIdAndGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }

        //入队
        mqSender.sendMiaoshaMessage(
                MiaoshaMessage.builder()
                        .user(user)
                        .goodsId(goodsId)
                        .build());

        return Result.success(0);


        //1.判断库存
        /*GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        Integer stock = goods.getStockCount();
        if (stock <= 0) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }*/


        //3.事务操作：减库存、下订单、写入秒杀订单
        /*OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
         *//*        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("goods", goods);*//*
        log.info(JSON.toJSONString(orderInfo));
        //return Result.success(orderInfo);
        return Result.<OrderInfo>builder().code(0).data(orderInfo).build();*/

    }

    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model, MiaoshaUser user,
                                      @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
        return Result.success(result);
    }

    /**
     * @Description:
     * 秒杀路径隐藏：
     * 前端点击立即秒杀，在服务端会发起一个获取path的请求，获取到path并存入缓存（存入userId + goodsId）后返回前端，才会发起一个秒杀请求，并且将path携带在参数中，
     * 秒杀请求验证path正确后才会进行秒杀流程
     *
     *  同时在该请求中也会验证图形验证码码是否正确

     */

    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request, MiaoshaUser user,
                                         @RequestParam("goodsId") long goodsId,
                                         @RequestParam(value = "verifyCode", defaultValue = "0") int verifyCode) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
        if (!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path = miaoshaService.createMiaoshaPath(user, goodsId);

        return Result.success(path);
    }


    /**
     * @Description:
     *
     * 在原来秒杀进行中、倒计时的地方加上一个请求验证码
     * 每当用户来到商品详情 要秒杀的时候，就会请求服务端生成一个图形验证码返回页面 并存在缓存中（userId goodsId + verifyCode）
     * 用户输入完后点击立即秒杀，会将验证码带入path请求，并验证（从缓存中取）是否和之前的结果一致，如果不一致则返回错误，一致则可以请求秒杀接口
     *
     * 图形验证码既可以防止机器人刷请求，也可以起到很好的减轻服务器压力的作用，10秒钟请求10000次与1秒请求10000次，对服务器的压力相差非常大
     *
     */
    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCod(HttpServletResponse response, MiaoshaUser user,
                                              @RequestParam("goodsId")long goodsId) {
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        try {
            BufferedImage image  = miaoshaService.createVerifyCode(user, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            return null;
        }catch(Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }

}

