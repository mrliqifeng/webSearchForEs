package com.gy.cpcsearch.service;

import com.alibaba.fastjson.JSONObject;
import com.gy.cpcsearch.mapper.FieldInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FieldInfoService {
    @Autowired
    FieldInfoMapper fieldInfoMapper;

    List<String> findFieldNameByTable(String tableName){
        if(tableName==null||tableName.equals("")){
            return fieldInfoMapper.findFieldName();
        }
        if(tableName.contains(",")){
            List<String> tablesList = new ArrayList<>();
            for(String oneTableName:tableName.split(",")){
                tablesList.addAll(fieldInfoMapper.findFieldNameByTable(oneTableName));
            }
            return tablesList.stream().distinct().collect(Collectors.toList());
        }
        return fieldInfoMapper.findFieldNameByTable(tableName);
    }

    String findDesByFieldAndTable(String tableName,String fieldName){
        return fieldInfoMapper.findDesByFieldAndTable(tableName,fieldName);
    };

    public JSONObject findTagInfoByTableName(String tableName){
        Map tagInfoMap = fieldInfoMapper.findTagInfoByTableName(tableName);
        JSONObject resultJson = new JSONObject();
        resultJson.put(tagInfoMap.get("tableName").toString(),tagInfoMap.get("fieldNames").toString().split(","));
        return resultJson;
    };

}
