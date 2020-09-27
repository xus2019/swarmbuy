package com.inotee.swarmbuy.mapper;

import com.inotee.swarmbuy.entity.MiaoshaUser;
import com.inotee.swarmbuy.entity.MiaoshaUserExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MiaoshaUserMapper {
    long countByExample(MiaoshaUserExample example);

    int deleteByExample(MiaoshaUserExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MiaoshaUser record);

    int insertSelective(MiaoshaUser record);

    MiaoshaUser selectOneByExample(MiaoshaUserExample example);

    List<MiaoshaUser> selectByExample(MiaoshaUserExample example);

    MiaoshaUser selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MiaoshaUser record, @Param("example") MiaoshaUserExample example);

    int updateByExample(@Param("record") MiaoshaUser record, @Param("example") MiaoshaUserExample example);

    int updateByPrimaryKeySelective(MiaoshaUser record);

    int updateByPrimaryKey(MiaoshaUser record);

    int batchInsert(@Param("list") List<MiaoshaUser> list);

    int batchInsertSelective(@Param("list") List<MiaoshaUser> list, @Param("selective") MiaoshaUser.Column ... selective);
}