package com.inotee.swarmbuy.service.impl;

import com.inotee.swarmbuy.common.util.MD5Util;
import com.inotee.swarmbuy.common.util.RedisUtil;
import com.inotee.swarmbuy.common.util.UUIDUtil;
import com.inotee.swarmbuy.entity.MiaoshaOrder;
import com.inotee.swarmbuy.entity.MiaoshaUser;
import com.inotee.swarmbuy.entity.OrderInfo;
import com.inotee.swarmbuy.entity.vo.GoodsVo;
import com.inotee.swarmbuy.service.GoodsService;
import com.inotee.swarmbuy.service.MiaoshaService;
import com.inotee.swarmbuy.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class MiaoshaServiceImpl implements MiaoshaService {


    @Resource
    GoodsService goodsService;

    @Resource
    OrderService orderService;

    @Resource
    RedisUtil redisUtil;

    //事务操作：减库存、下订单、写入秒杀订单
    @Override
    @Transactional
    public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {

        boolean success = goodsService.reduceStock(goods);
        if (success) {
            return orderService.createMiaoshaOrder(user, goods);
        } else {
            redisUtil.set("isMiaoshaOver" + goods.getId(), true);
            return null;
        }


    }

    @Override
    public long getMiaoshaResult(Long userId, long goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaOrderInfoByUserIdAndGoodsId(userId, goodsId);
        if (order != null) {//秒杀成功
            return order.getOrderId();
        } else {
            boolean isOver = redisUtil.hasKey("isMiaoshaOver" + goodsId);
            return isOver ? -1 : 0;
        }
    }

    //--------------------------------隐藏秒杀路径--------------------------------
    @Override
    public String createMiaoshaPath(MiaoshaUser user, long goodsId) {
        if(user == null || goodsId <=0) {
            return null;
        }
        String str = MD5Util.md5(UUIDUtil.uuid()+"123456");
        redisUtil.set("miaoshaPath:"+user.getId()+"_"+goodsId,str,60);
        return str;
    }

    @Override
    public boolean checkPath(MiaoshaUser user, long goodsId, String path) {
        if(user == null || path == null) {
            return false;
        }
        String pathOld= (String) redisUtil.get("miaoshaPath:"+user.getId()+"_"+goodsId);

        return path.equals(pathOld);
    }


    //--------------------------------图形验证码--------------------------------

    @Override
    public BufferedImage createVerifyCode(MiaoshaUser user, long goodsId) {
        if(user == null || goodsId <=0) {
            return null;
        }
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisUtil.set("miaoshaVerifyCode:"+ user.getId()+","+goodsId, rnd);
        //输出图片
        return image;
    }

    private static char[] ops = new char[] {'+', '-', '*'};
    /**
     * + - *
     * */
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+ num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    private static int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer)engine.eval(exp);
        }catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    @Override
    public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
        if(user == null || goodsId <=0) {
            return false;
        }
        Integer oldCode =(Integer)redisUtil.get("miaoshaVerifyCode:"+ user.getId()+","+goodsId);
        if(oldCode == null || oldCode - verifyCode != 0 ) {
            return false;
        }
        redisUtil.del("miaoshaVerifyCode:"+ user.getId()+","+goodsId);
        return true;
    }
}
