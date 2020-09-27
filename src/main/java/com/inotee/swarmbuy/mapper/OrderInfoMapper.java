package com.inotee.swarmbuy.mapper;

import com.inotee.swarmbuy.entity.OrderInfo;
import com.inotee.swarmbuy.entity.OrderInfoExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderInfoMapper {
    long countByExample(OrderInfoExample example);

    int deleteByExample(OrderInfoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(OrderInfo record);

    int insertSelective(OrderInfo record);

    OrderInfo selectOneByExample(OrderInfoExample example);

    List<OrderInfo> selectByExample(OrderInfoExample example);

    OrderInfo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") OrderInfo record, @Param("example") OrderInfoExample example);

    int updateByExample(@Param("record") OrderInfo record, @Param("example") OrderInfoExample example);

    int updateByPrimaryKeySelective(OrderInfo record);

    int updateByPrimaryKey(OrderInfo record);

    int batchInsert(@Param("list") List<OrderInfo> list);

    int batchInsertSelective(@Param("list") List<OrderInfo> list, @Param("selective") OrderInfo.Column ... selective);
}