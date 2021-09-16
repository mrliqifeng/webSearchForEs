package com.gy.cpcsearch.mapper;

import com.gy.cpcsearch.entity.FieldInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface FieldInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(FieldInfo record);

    int insertSelective(FieldInfo record);

    FieldInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(FieldInfo record);

    int updateByPrimaryKey(FieldInfo record);

    List<String> findFieldName();

    List<String> findFieldNameByTable(@Param("tableName") String tableName);

    String findDesByFieldAndTable(@Param("tableName") String tableName,@Param("fieldName") String fieldName);

    List<Map> getAllTagInfo();

    Map findTagInfoByTableName(@Param("tableName") String tableName);

}