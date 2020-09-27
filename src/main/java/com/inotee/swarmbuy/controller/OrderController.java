package com.inotee.swarmbuy.controller;

import com.inotee.swarmbuy.entity.MiaoshaUser;
import com.inotee.swarmbuy.entity.OrderInfo;
import com.inotee.swarmbuy.entity.result.CodeMsg;
import com.inotee.swarmbuy.entity.result.Result;
import com.inotee.swarmbuy.entity.vo.GoodsVo;
import com.inotee.swarmbuy.entity.vo.OrderDetailVo;
import com.inotee.swarmbuy.service.GoodsService;
import com.inotee.swarmbuy.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @Auther: xushuai56
 * @Date: 2020/9/6 16:10
 * @Description:
 */

@Controller
@RequestMapping("/order")
public class OrderController {

    @Resource
    OrderService orderService;

    @Resource
    GoodsService goodsService;

    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, MiaoshaUser user,
                                      @RequestParam("orderId") long orderId) {
        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        OrderInfo order = orderService.getOrderById(orderId);
        if(order == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        long goodsId = order.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);

        OrderDetailVo build = OrderDetailVo.builder().goods(goods).order(order).build();
        return Result.success(build);
    }
}
