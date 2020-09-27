package com.inotee.swarmbuy.mapper;

import com.inotee.swarmbuy.entity.MiaoshaGoods;
import org.apache.ibatis.annotations.Update;

/**
 * @Auther: xushuai56
 * @Date: 2020/9/6 17:48
 * @Description:
 */
public interface ReduceStockMapper {
    @Update("update miaosha_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count > 0")
    public int reduceStock(MiaoshaGoods g);
}
