package com.gy.cpcsearch.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gy.cpcsearch.entity.TableInfo;
import com.gy.cpcsearch.mapper.TableInfoMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TableInfoService {
    @Autowired
    TableInfoMapper tableInfoMapper;

    public String getAliasByName(String name){
        return tableInfoMapper.getAliasByName(name);
    };

    public String getNameByAlias(String alias){
        if(!alias.contains(",")){
            return tableInfoMapper.getNameByAlias(alias);
        } else{
            StringBuilder indexNames = new StringBuilder();
            String [] aliases = alias.split(",");
            for(String oneAlias:aliases){
                String indexName = tableInfoMapper.getNameByAlias(oneAlias);
                indexNames.append(indexName);
                indexNames.append(",");
            }
            return indexNames.toString();
        }
    };

    public List<String> findAllName(){
        return tableInfoMapper.findAllName();
    };

    public JSONArray getAliasType(){
        List<Map> tableMap = tableInfoMapper.getAliasType();
        JSONArray jsonArray = (JSONArray) JSON.toJSON(tableMap);
        JSONArray resultArray = new JSONArray();
        JSONObject quanku = new JSONObject();
        JSONArray quankuArray = new JSONArray();
        quankuArray.add("全库");
        quanku.put("全库",quankuArray);
        resultArray.add(quanku);
        for(int i =0;i<jsonArray.size();i++){
            JSONObject resultJson = new JSONObject();
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            resultJson.put(jsonObject.getString("tType"),jsonObject.getString("aliases").split(","));
            resultArray.add(resultJson);
        }
        resultArray.add(resultArray.remove(2));
        resultArray.add(resultArray.remove(4));
        return resultArray;
    }

}

