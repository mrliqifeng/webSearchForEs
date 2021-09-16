package com.gy.cpcsearch.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gy.cpcsearch.entity.AggInfo;
import com.gy.cpcsearch.service.*;

import com.gy.cpcsearch.utils.ConfUtil;
import com.gy.cpcsearch.utils.ElasticUtil;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
public class SearchController {
    @Autowired
    SearchService searchService;

    @Autowired
    TableInfoService tableInfoService;

    @Autowired
    FieldInfoService fieldInfoService;

    @Autowired
    AggInfoService aggInfoService;

    @Autowired
    ElasticUtil elasticUtil;

    @Autowired
    ConfUtil confUtil;

    @GetMapping("getList")
    public JSONObject getList(@RequestParam(value = "search_value") String searchValue,
                              @RequestParam(value = "source_name", required = false) String sourceName,
                              @RequestParam(value = "page", required = false) Integer startPage,
                              @RequestParam(value = "page_size", required = false) Integer pageSize,
                              @RequestParam(value = "start_time", required = false) String startTime,
                              @RequestParam(value = "end_time", required = false) String endTime,
                              @RequestParam(value = "sort", required = false) String sortString){
        long start = System.currentTimeMillis();
        if(searchValue.contains("\\")){
            searchValue = searchValue.replace("\\","\\\\");
        }
        String indexName = tableInfoService.getNameByAlias(sourceName);
        if(!sourceName.equals("") && indexName == null){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("code",-1);
            jsonObject.put("error","当前数据来源不存在");
            return jsonObject;
        }
        if(pageSize>100){
            pageSize = 100;
        }
        JSONObject jsonObject = searchService.getResultJson(indexName,searchValue,startPage,pageSize,startTime,endTime,sortString);
        jsonObject.put("page",1);;
        if(startPage!=null&&startPage>0){
            jsonObject.put("page",startPage);
        }
        jsonObject.put("code",1);
        long stop = System.currentTimeMillis();
        System.out.println("时长："+(stop-start));
        return jsonObject;
    }

    /**
     * 获取数据详情
     * @param sourceName 数据来源名称
     * @param idString 数据id值
     * @return 数据详情json
     */
    @GetMapping("getDetail")
    public JSONObject getDetail(@RequestParam(value = "source_name") String sourceName,
                                @RequestParam(value = "id", required = false) String idString){
        String indexName = tableInfoService.getNameByAlias(sourceName);
        return elasticUtil.getById(indexName,idString);
    }

    /**
     * 获取聚合统计数据
     * @param sourceName 数据来源名称
     * @param fieldString 聚合字段名称
     * @return 聚合结果json
     */
    @GetMapping("getAggregate")
    public JSONObject getAggregate(@RequestParam(value = "source_name", required = false) String sourceName,
                                   @RequestParam(value = "field_name", required = false) String fieldString){
        String tableName = tableInfoService.getNameByAlias(sourceName);
        AggInfo aggInfo = aggInfoService.getFieldByTableAndAlias(tableName,fieldString);
        String urlString = String.format("http://%s:%d/%s/_search",confUtil.esIp,confUtil.esPort,tableName);
        String searchJson = String.format("{\"size\": 0,\"aggs\":{\"all_interests\":{\"terms\":{\"field\":\"%s.keyword\",\"size\": %d}}}}",aggInfo.getaField(),aggInfo.getaLength());
        HttpResponse<String> response = null;
        try {
            response = Unirest.post(urlString)
                    .header("Content-Type", "application/json")
                    .body(searchJson).asString();
            JSONArray jsonArray = JSONObject.parseObject(response.getBody()).getJSONObject("aggregations").getJSONObject("all_interests").getJSONArray("buckets");
            JSONArray resultArray = new JSONArray();
            JSONObject resultJson = new JSONObject();
            JSONObject tmpJson = new JSONObject();
            tmpJson.put("key","其他");
            tmpJson.put("doc_count",0);
            for(int i = 0;i<jsonArray.size();i++){
                JSONObject oneJson = jsonArray.getJSONObject(i);
                if(i<8){
                    resultArray.add(oneJson);
                } else{
                    tmpJson.put("doc_count",tmpJson.getInteger("doc_count")+oneJson.getInteger("doc_count"));
                }
            }
            if(tmpJson.getInteger("doc_count")!=0){
                resultArray.add(tmpJson);
            }
            resultJson.put("show_data",resultArray);
            resultJson.put("src_data",jsonArray);
            resultJson.put("source_name",sourceName);
            resultJson.put("field_name",fieldString);
            return resultJson;
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return null;
    }
}
