package com.gy.cpcsearch.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gy.cpcsearch.utils.ConfUtil;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author Liqifeng
 * 用于处理搜索数据
 */
@Service
public class SearchService {
    @Autowired
    FieldInfoService fieldInfoService;

    @Autowired
    TableInfoService tableInfoService;

    @Autowired
    AggInfoService aggInfoService;

    @Autowired
    ConfUtil confUtil;

    private JSONObject aliasJson = new JSONObject();

    class CountHttpClass extends Thread{
        String countSearchJson;
        String oneTableName;
        volatile JSONObject indexCountJson;
        public CountHttpClass(String countSearchJson,String oneTableName,JSONObject indexCountJson){
            this.oneTableName = oneTableName;
            this.countSearchJson = countSearchJson;
            this.indexCountJson = indexCountJson;
        }
        @Override
        public void run() {
            String urlString2 = String.format("http://%s:%d/%s/_search",confUtil.esIp,confUtil.esPort,oneTableName);
            try {
                HttpResponse<String> response2 = Unirest.post(urlString2)
                        .header("Content-Type", "application/json")
                        .body(countSearchJson).asString();
                Integer value = 0;
                try {
                    value = JSONObject.parseObject(response2.getBody()).getJSONObject("hits").getJSONObject("total").getInteger("value");
                } catch (Exception e){
                    System.out.println(urlString2);
                }
                String name = tableInfoService.getAliasByName(oneTableName);
                if(name==null||name.equals("")){
                    name = tableInfoService.getAliasByName(oneTableName);
                }
                indexCountJson.put(name,value);
            } catch (UnirestException e) {
                e.printStackTrace();
            }
        }
    }

    public String getJsonStringRec(JSONObject jsonObject, String keyString){
        return getJsonStringRec(jsonObject,keyString,0);
    }

    public String getJsonStringRec(JSONObject jsonObject, String keyString, int i){
        String key = keyString.split("\\.")[i];
        try {
            JSONObject resultJson = jsonObject.getJSONObject(key);
            return getJsonStringRec(resultJson,keyString,i+1);
        } catch (Exception e){
            String outString = jsonObject.getString(key);
            try{
                JSONArray jsonArray = JSONArray.parseArray(outString);
                String [] keyArray = keyString.split("\\.");
                StringBuilder stringBuffer = new StringBuilder();
                for(int z = 0;z<jsonArray.size();z++){
                    String out = jsonArray.getJSONObject(z).getString(keyArray[keyArray.length-1]);
                    stringBuffer.append(out);
                    if(z!=jsonArray.size()-1){
                        stringBuffer.append(",");
                    }
                }
                return stringBuffer.toString();
            } catch (Exception e2){
                return jsonObject.getString(key);
            }
        }
    }

    /**
     * 调整数据格式
     * @param jsonArray 原始数据格式
     * @return 调整过的数据格式
     */
    private JSONArray getResultArray(JSONArray jsonArray){
        JSONArray resultAyyay = new JSONArray();
        HashMap<String,List<String>> fieldNameHash = new HashMap<>();
        JSONObject dataNameJson = new JSONObject();
        for(int i = 0;i<jsonArray.size();i++){
            JSONObject dataJson = new JSONObject();
            //获取单独的一条数据
            JSONObject oneJson = jsonArray.getJSONObject(i);
            String idString = oneJson.getString("_id");
            //获取数据中的详情数据
            JSONObject sourceJson = oneJson.getJSONObject("_source");
            //获取索引名称
            String indexName = oneJson.getString("_index");
            String aliasName = aliasJson.getString(indexName);
            if(aliasName==null){
                aliasName = tableInfoService.getAliasByName(indexName);
                aliasJson.put(indexName,aliasName);
            }
            //根据索引名称查找别名
            List<String> fieldNames = fieldNameHash.get(indexName);
            if(fieldNames==null){
                fieldNames = fieldInfoService.findFieldNameByTable(indexName);
                fieldNameHash.put(indexName,fieldNames);
            }
            JSONArray valueArray = new JSONArray();
            //获取每个字段的值
            for(String fieldName :fieldNames){
                String fieldValue = getJsonStringRec(sourceJson,fieldName);
                if(fieldValue!=null&&!fieldValue.equals("")){
                    JSONObject valueJson = new JSONObject();
                    String dataName = dataNameJson.getString(indexName+"_"+fieldName);
                    if(dataName==null){
                        dataName = fieldInfoService.findDesByFieldAndTable(indexName,fieldName);
                        dataNameJson.put(indexName+"_"+fieldName,dataName);
                    }
                    valueJson.put("data_name",dataName);
                    if(fieldValue.length()>=46){
                        fieldValue = fieldValue.substring(0,46)+"...";
                    }
                    valueJson.put("data_value",fieldValue);
                    valueArray.add(valueJson);
                }
            }
            dataJson.put("offical_table","");
            dataJson.put("id",idString);
            dataJson.put("value",valueArray);
            dataJson.put("source_name",aliasName);
            resultAyyay.add(dataJson);
        }
        return resultAyyay;
    }


    /**
     * 调整结果字段格式
     * @param indexName 索引名称
     * @param searchValue 搜索词
     * @param page 页数
     * @param pageSize 数据量
     * @return 结果数据
     */
    public JSONObject getResultJson(String indexName,String searchValue, Integer page, Integer pageSize,String startTime,String endTime,String sortString){
        JSONObject searchResult = search(indexName,searchValue,page,pageSize,startTime,endTime,sortString);
        JSONObject resultJson = new JSONObject();
        //如果当前库表为空，则返回以下信息
        if(searchResult==null){
            resultJson.put("count",0);
            resultJson.put("page_size",0);
            resultJson.put("result_data",new JSONArray());
            return resultJson;
        }

        JSONObject countIndexJson = null;
        if(!searchValue.trim().equals("")){
            List<String> allTableNameList = tableInfoService.findAllName();
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(allTableNameList.size());
            String countSearchJson = getSearchString(null,searchValue,0,1,startTime,endTime,sortString);
            countIndexJson = new JSONObject();
            for(String oneTableName:allTableNameList){
                CountHttpClass countHttpClass = new CountHttpClass(countSearchJson,oneTableName,countIndexJson);
                executor.execute(countHttpClass);
            }
            executor.shutdown();
            while(true){
                boolean isend = executor.isTerminated();
                if(isend){
                    break;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        JSONArray sourceArray = tableInfoService.getAliasType();
        if(countIndexJson!=null){
            JSONArray newSourceArrray = new JSONArray();
            for(int i = 0;i<sourceArray.size();i++){
                JSONObject jsonObject = sourceArray.getJSONObject(i);
                String keyString = (String)jsonObject.keySet().toArray()[0];
                if(keyString.equals("全库")){
                    continue;
                }
                JSONArray aliasArray = jsonObject.getJSONArray(keyString);
                Integer keyCount = 0;
                JSONArray newAliasArray = new JSONArray();
                for(int j=0;j<aliasArray.size();j++){
                    String aliasName = aliasArray.getString(j);
                    Integer oneIndexCount = countIndexJson.getIntValue(aliasName);
                    keyCount += oneIndexCount;
                    if(oneIndexCount!=0){
                        newAliasArray.add(aliasName+"-"+oneIndexCount);
                    } else{
                        newAliasArray.add(aliasName);
                    }
                }
                if(keyCount!=0){
                    jsonObject.remove(keyString);
                    jsonObject.put(keyString+"-"+keyCount,newAliasArray);
                }
                newSourceArrray.add(jsonObject);
            }
        }

        resultJson.put("all_source",sourceArray);
        int count = searchResult.getJSONObject("total").getInteger("value");
        resultJson.put("count",count);
        JSONArray aggStrings = aggInfoService.getAliasByTable(indexName);
        resultJson.put("agg_info",new JSONArray());
        if(aggStrings!=null&&aggStrings.size()!=0){
            resultJson.put("agg_info",aggStrings);
        }
        JSONArray jsonArray = searchResult.getJSONArray("hits");
        resultJson.put("page_size",jsonArray.size());
        JSONArray resultArray = getResultArray(jsonArray);
        resultJson.put("result_data",resultArray);
        return resultJson;
    }

    /**
     * 构建搜索参数
     * @param indexName 索引名称
     * @param searchString 具体搜索词
     * @param from 起始页
     * @param pageSize 页面数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param sortString 排序字段
     * @return 搜索参数
     */
    public String getSearchString(String indexName,String searchString, int from , int pageSize, String startTime,String endTime,String sortString){
        List<String> fieldList = fieldInfoService.findFieldNameByTable(indexName);
        fieldList.add("hash_value");
        JSONObject searchJson = new JSONObject();
        searchJson.put("from",from);
        searchJson.put("size",pageSize);
        searchJson.put("track_total_hits",true);
        JSONObject queryJson = new JSONObject();
        JSONObject boolJson = new JSONObject();
        JSONArray zuheArray = new JSONArray();
        //构建搜索条件
        if((searchString.contains("&&")||searchString.contains("||"))&&!(searchString.contains("&&")&&searchString.contains("||"))){
            String [] splitString = new String[]{};
            String zuheTiaojian = "";
            if(searchString.contains("&&")){
                splitString = searchString.split("&&");
                zuheTiaojian = "must";
            }
            if(searchString.contains("||")){
                splitString = searchString.split("\\|\\|");
                zuheTiaojian = "should";
            }
            for(String oneSearchString:splitString){
                JSONObject queryStringJson = new JSONObject();
                JSONObject queryValue = new JSONObject();
                if(oneSearchString.contains("==")){
                    String fieldName = oneSearchString.split("==")[0];
                    String queryString = oneSearchString.split("==")[1];
                    queryValue.put("query","\""+queryString+"\"");
                    queryValue.put("default_field",fieldName);
                } else{
                    queryValue.put("query","\""+oneSearchString+"\"");
                }
                queryStringJson.put("query_string",queryValue);
                zuheArray.add(queryStringJson);
            }
            boolJson.put(zuheTiaojian,zuheArray);
        } else{
            if(searchString.trim().equals("")){
                boolJson.put("must",zuheArray);
            } else{
                JSONObject queryStringJson = new JSONObject();
                JSONObject queryValue = new JSONObject();
                if(searchString.contains("==")){
                    String fieldName = searchString.split("==")[0];
                    String queryString = searchString.split("==")[1];
                    queryValue.put("query","\""+queryString+"\"");
                    queryValue.put("default_field",fieldName);
                } else{
                    queryValue.put("query","\""+searchString+"\"");
                }
                //匹配查询
                if(searchString.startsWith("*")&&searchString.endsWith("*")){
                    JSONObject queryStringJson2 = new JSONObject();
                    JSONObject queryValue2 = new JSONObject();
                    if(searchString.contains("==")){
                        String fieldName = searchString.split("==")[0];
                        String queryString = searchString.split("==")[1];
                        queryValue2.put("query","*"+queryString+"*");
                        queryValue2.put("default_field",fieldName);
                    } else{
                        queryValue2.put("query","*"+searchString+"*");
                    }
                    queryStringJson2.put("query_string",queryValue2);
                    zuheArray.add(queryStringJson2);
                }
                queryStringJson.put("query_string",queryValue);
                zuheArray.add(queryStringJson);
                boolJson.put("should",zuheArray);
            }
        }
        JSONObject timeFilterJson = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeString = "";
        if((startTime!=null&&!startTime.equals(""))&&(endTime!=null&&!endTime.equals(""))){
            timeString = String.format("{\"range\": {\"%s\": {\"gt\":startTime,\"lt\":endTime}}}",confUtil.timeFilterField);
            Date startDate = null;
            Date endDate = null;
            try {
                startDate = simpleDateFormat.parse(startTime);
                endDate = simpleDateFormat.parse(endTime);
                timeString = timeString.replace("startTime",Long.toString(startDate.getTime()));
                timeString = timeString.replace("endTime",Long.toString(endDate.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if((startTime==null||startTime.equals(""))&&(endTime!=null&&!endTime.equals(""))){
            timeString = String.format("{\"range\": {\"%s\": {\"lt\":endTime}}}",confUtil.timeFilterField);
            Date endDate = null;
            try {
                endDate = simpleDateFormat.parse(endTime);
                timeString = timeString.replace("endTime",Long.toString(endDate.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if((startTime!=null&&!startTime.equals(""))&&(endTime==null||endTime.equals(""))){
            timeString = String.format("{\"range\": {\"%s\": {\"gt\":startTime}}}",confUtil.timeFilterField);
            Date startDate = null;
            try {
                startDate = simpleDateFormat.parse(startTime);
                timeString = timeString.replace("startTime",Long.toString(startDate.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        timeFilterJson = JSON.parseObject(timeString);
        if(timeFilterJson!=null&&confUtil.isTimeFfilter==1){
            JSONArray mustArray = boolJson.getJSONArray("must");
            if (mustArray == null || mustArray.size() == 0) {
                JSONArray jsonArray = new JSONArray();
                jsonArray.add(timeFilterJson);
                boolJson.put("must",jsonArray);
            } else{
                mustArray.add(timeFilterJson);
            }
        }
        queryJson.put("bool",boolJson);
        searchJson.put("query",queryJson);

        if(confUtil.isSort==1){
            if(sortString==null||sortString.equals("")){
                sortString = "desc";
            }
            String paixuString = String.format("[{\"%s\":{\"order\":\""+sortString+"\"}}]",confUtil.sortField);
            JSONArray paixuArray = (JSONArray) JSONArray.parse(paixuString);
            searchJson.put("sort",paixuArray);
        }


        searchJson.put("_source",fieldList);
        return searchJson.toString();
    }

    /**
     * 根据数据库名称与搜索字段获取数据
     * @param indexName 索引名称
     * @param searchValue 搜索词
     * @param page 当前页数
     * @param pageSize 数据量
     * @return 搜索结果
     */
    public JSONObject search(String indexName,String searchValue,Integer page,Integer pageSize,String startTime,String endTime,String desc){
        int from = 0;
        if((page!=null&&pageSize!=null)&&(page > 0 && pageSize > 0)){
            from = ((page-1)*pageSize);
        } else{
            pageSize = 10;
        }
        Unirest.setTimeouts(0, 0);
        String searchJson = getSearchString(indexName,searchValue,from,pageSize,startTime,endTime,desc);
        String urlString = "";

        if(indexName!=null&&!indexName.equals("")){
            urlString = String.format("http://%s:%d/%s/_search",confUtil.esIp,confUtil.esPort,indexName);
        } else{
            String allTableName = tableInfoService.findAllName().stream().collect(Collectors.joining(","));
            urlString = String.format("http://%s:%d/",confUtil.esIp,confUtil.esPort)+allTableName+"/_search";
        }
        HttpResponse<String> response = null;
        try {
            response = Unirest.post(urlString)
                    .header("Content-Type", "application/json")
                    .body(searchJson).asString();
            return JSONObject.parseObject(response.getBody()).getJSONObject("hits");
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return null;
    }

}
