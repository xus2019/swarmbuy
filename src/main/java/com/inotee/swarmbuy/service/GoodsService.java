package com.inotee.swarmbuy.service;

import com.inotee.swarmbuy.entity.vo.GoodsVo;

import java.util.List;

public interface GoodsService {
    List<GoodsVo> queryGoodsList();

    GoodsVo getGoodsVoByGoodsId(long goodsId);

    Boolean reduceStock(GoodsVo goods);
}
