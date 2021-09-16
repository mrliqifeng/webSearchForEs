package com.gy.cpcsearch.mapper;

import com.gy.cpcsearch.entity.AggInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AggInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(AggInfo record);

    int insertSelective(AggInfo record);

    AggInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(AggInfo record);

    int updateByPrimaryKey(AggInfo record);

    AggInfo getFieldByTableAndAlias(@Param("tableName") String tableName, @Param("alias") String alias);

    List<String> getAliasByTable(@Param("tableName") String tableName);
}