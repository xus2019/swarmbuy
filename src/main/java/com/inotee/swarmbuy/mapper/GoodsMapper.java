package com.inotee.swarmbuy.mapper;

import com.inotee.swarmbuy.entity.Goods;
import com.inotee.swarmbuy.entity.GoodsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface GoodsMapper {
    long countByExample(GoodsExample example);

    int deleteByExample(GoodsExample example);

    int deleteByPrimaryKey(Long id);

    int insert(Goods record);

    int insertSelective(Goods record);

    Goods selectOneByExample(GoodsExample example);

    Goods selectOneByExampleWithBLOBs(GoodsExample example);

    List<Goods> selectByExampleWithBLOBs(GoodsExample example);

    List<Goods> selectByExample(GoodsExample example);

    Goods selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Goods record, @Param("example") GoodsExample example);

    int updateByExampleWithBLOBs(@Param("record") Goods record, @Param("example") GoodsExample example);

    int updateByExample(@Param("record") Goods record, @Param("example") GoodsExample example);

    int updateByPrimaryKeySelective(Goods record);

    int updateByPrimaryKeyWithBLOBs(Goods record);

    int updateByPrimaryKey(Goods record);

    int batchInsert(@Param("list") List<Goods> list);

    int batchInsertSelective(@Param("list") List<Goods> list, @Param("selective") Goods.Column ... selective);
}