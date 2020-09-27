package com.inotee.swarmbuy.service.impl;

import com.inotee.swarmbuy.common.exception.GlobalException;
import com.inotee.swarmbuy.entity.Goods;
import com.inotee.swarmbuy.entity.GoodsExample;
import com.inotee.swarmbuy.entity.MiaoshaGoods;
import com.inotee.swarmbuy.entity.MiaoshaGoodsExample;
import com.inotee.swarmbuy.entity.result.CodeMsg;
import com.inotee.swarmbuy.entity.vo.GoodsVo;
import com.inotee.swarmbuy.mapper.GoodsMapper;
import com.inotee.swarmbuy.mapper.MiaoshaGoodsMapper;
import com.inotee.swarmbuy.mapper.ReduceStockMapper;
import com.inotee.swarmbuy.service.GoodsService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Resource
    MiaoshaGoodsMapper miaoshaGoodsMapper;

    @Resource
    GoodsMapper goodsMapper;

    @Resource
    ReduceStockMapper reduceStockMapper;

    @Override
    public List<GoodsVo> queryGoodsList() {

        MiaoshaGoodsExample example = new MiaoshaGoodsExample();
        List<MiaoshaGoods> miaoshaGoodsList = miaoshaGoodsMapper.selectByExample(example);
        List<GoodsVo> result = new ArrayList<>();
        for (MiaoshaGoods miaoshaGoods : miaoshaGoodsList) {

            GoodsExample goodsExample = new GoodsExample();
            goodsExample.createCriteria().andIdEqualTo(miaoshaGoods.getGoodsId());
            List<Goods> goods = goodsMapper.selectByExample(goodsExample);
            if (CollectionUtils.isEmpty(goods)) {
                throw new GlobalException(CodeMsg.GOODS_ID_ERROR);
            }
            Goods good = goods.get(0);

            result.add(GoodsVo.builder()
                    .id(good.getId())
                    .goodsName(good.getGoodsName())
                    .goodsImg(good.getGoodsImg())
                    .goodsPrice(good.getGoodsPrice().doubleValue())
                    .miaoshaPrice(miaoshaGoods.getMiaoshaPrice().doubleValue())
                    .stockCount(miaoshaGoods.getStockCount())
                    .startDate(miaoshaGoods.getStartDate())
                    .endDate(miaoshaGoods.getEndDate())
                    .build());
        }
        return result;
    }

    @Override
    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        List<GoodsVo> goodsVos = this.queryGoodsList();
        for (GoodsVo good : goodsVos) {
            if (good.getId().equals(goodsId)) {
                return good;
            }
        }
        return new GoodsVo();
    }


    public void oldReduceStock(GoodsVo goods) {
        MiaoshaGoodsExample example = new MiaoshaGoodsExample();
        example.createCriteria()
                .andGoodsIdEqualTo(goods.getId());
        MiaoshaGoods miaoshaGoods = miaoshaGoodsMapper.selectByExample(example).get(0);
        Integer oldStock = miaoshaGoods.getStockCount();

        MiaoshaGoods record = new MiaoshaGoods();
        record.setStockCount(oldStock - 1);

        miaoshaGoodsMapper.updateByExampleSelective(record, example);
    }

    @Override
    public Boolean reduceStock(GoodsVo goods) {
        MiaoshaGoods g = new MiaoshaGoods();
        g.setGoodsId(goods.getId());
        int result =reduceStockMapper.reduceStock(g);
        if (result >= 1) {
            return true;
        }
        return false;
    }


}
