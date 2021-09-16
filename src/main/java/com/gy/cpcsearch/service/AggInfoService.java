package com.gy.cpcsearch.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.gy.cpcsearch.entity.AggInfo;
import com.gy.cpcsearch.mapper.AggInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AggInfoService {
    @Autowired
    AggInfoMapper aggInfoMapper;


    public AggInfo getFieldByTableAndAlias(String tableName, String alias){
        return aggInfoMapper.getFieldByTableAndAlias(tableName,alias);
    };

    public JSONArray getAliasByTable(String tableName){
        return (JSONArray)JSON.toJSON(aggInfoMapper.getAliasByTable(tableName));
    };
}
