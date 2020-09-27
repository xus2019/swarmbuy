package com.inotee.swarmbuy.mapper;

import com.inotee.swarmbuy.entity.MiaoshaOrder;
import com.inotee.swarmbuy.entity.MiaoshaOrderExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MiaoshaOrderMapper {
    long countByExample(MiaoshaOrderExample example);

    int deleteByExample(MiaoshaOrderExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MiaoshaOrder record);

    int insertSelective(MiaoshaOrder record);

    MiaoshaOrder selectOneByExample(MiaoshaOrderExample example);

    List<MiaoshaOrder> selectByExample(MiaoshaOrderExample example);

    MiaoshaOrder selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MiaoshaOrder record, @Param("example") MiaoshaOrderExample example);

    int updateByExample(@Param("record") MiaoshaOrder record, @Param("example") MiaoshaOrderExample example);

    int updateByPrimaryKeySelective(MiaoshaOrder record);

    int updateByPrimaryKey(MiaoshaOrder record);

    int batchInsert(@Param("list") List<MiaoshaOrder> list);

    int batchInsertSelective(@Param("list") List<MiaoshaOrder> list, @Param("selective") MiaoshaOrder.Column ... selective);
}