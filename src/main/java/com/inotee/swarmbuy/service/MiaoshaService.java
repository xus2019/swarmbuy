package com.inotee.swarmbuy.service;

import com.inotee.swarmbuy.entity.MiaoshaUser;
import com.inotee.swarmbuy.entity.OrderInfo;
import com.inotee.swarmbuy.entity.vo.GoodsVo;

import java.awt.image.BufferedImage;

public interface MiaoshaService {
    OrderInfo miaosha(MiaoshaUser user, GoodsVo goods);

    long getMiaoshaResult(Long userId, long goodsId);

    String createMiaoshaPath(MiaoshaUser user, long goodsId);

    boolean checkPath(MiaoshaUser user, long goodsId, String path);

    BufferedImage createVerifyCode(MiaoshaUser user, long goodsId);

    boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode);
}
