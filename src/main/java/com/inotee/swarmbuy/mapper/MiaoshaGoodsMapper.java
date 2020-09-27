package com.inotee.swarmbuy.mapper;

import com.inotee.swarmbuy.entity.MiaoshaGoods;
import com.inotee.swarmbuy.entity.MiaoshaGoodsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MiaoshaGoodsMapper {
    long countByExample(MiaoshaGoodsExample example);

    int deleteByExample(MiaoshaGoodsExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MiaoshaGoods record);

    int insertSelective(MiaoshaGoods record);

    MiaoshaGoods selectOneByExample(MiaoshaGoodsExample example);

    List<MiaoshaGoods> selectByExample(MiaoshaGoodsExample example);

    MiaoshaGoods selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MiaoshaGoods record, @Param("example") MiaoshaGoodsExample example);

    int updateByExample(@Param("record") MiaoshaGoods record, @Param("example") MiaoshaGoodsExample example);

    int updateByPrimaryKeySelective(MiaoshaGoods record);

    int updateByPrimaryKey(MiaoshaGoods record);

    int batchInsert(@Param("list") List<MiaoshaGoods> list);

    int batchInsertSelective(@Param("list") List<MiaoshaGoods> list, @Param("selective") MiaoshaGoods.Column ... selective);
}