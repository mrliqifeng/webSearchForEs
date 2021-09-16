package com.gy.cpcsearch.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @author Liqifeng
 * 当前类为Elasticsearch工具类
 */

@Component
public class ElasticUtil {

    public RestHighLevelClient client;

    public RestHighLevelClient getClient() {
        return client;
    }

    @Autowired
    public ElasticUtil(ConfUtil confUtil) {
        client = new RestHighLevelClient(
                RestClient.builder(
                    new HttpHost(confUtil.esIp,confUtil.esPort, "http")
                ));
    }
    /**
     * 根据范围参数构建range请求
     *
     * @param rangeJson 范围参数
     * @return 构建好的请求
     */
    public RangeQueryBuilder getRange(JSONObject rangeJson) {
        String rangeString = rangeJson.getString("rangeString");
        if (rangeString != null) {
            RangeQueryBuilder rangeQuery = null;
            try {
                long minValue = rangeJson.getLong("minValue");
                rangeQuery = QueryBuilders.rangeQuery(rangeString);
                if (minValue != 0) {
                    rangeQuery.from(minValue);
                }
            } catch (Exception ignored) {
            }
            try {
                long maxValue = rangeJson.getLong("maxValue");
                if (rangeQuery == null) {
                    rangeQuery = QueryBuilders.rangeQuery(rangeString);
                }
                if (maxValue != 0) {
                    rangeQuery.to(maxValue);
                }
            } catch (Exception ignored) {
            }
            return rangeQuery;
        } else {
            return null;
        }
    }

    /**
     * 获取索引中的数据总条数
     *
     * @param index      索引名称
     * @param searchJson 搜索参数
     * @return 数据条数
     */
    public int getCount(String index, JSONObject searchJson, JSONObject rangeJson) {
        CountRequest countRequest = new CountRequest(index);
        CountResponse countResponse = null;
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        if (searchJson.keySet().size() != 0) {
            for (String keyString : searchJson.keySet()) {
                Object searchValue = searchJson.get(keyString);
                qb = qb.must(QueryBuilders.matchQuery(keyString, searchValue));
                //如果搜索对象为String类型，且模糊查询开启
//                qb = qb.must(QueryBuilders.matchQuery(keyString, searchValue).fuzziness(Fuzziness.AUTO));

            }
        }
        if (rangeJson.keySet().size() != 0) {
            RangeQueryBuilder rangeQueryBuilder = getRange(rangeJson);
            if (rangeQueryBuilder != null) {
                qb.must(rangeQueryBuilder);
            }
        }
        searchSourceBuilder.query(qb);
        countRequest.source(searchSourceBuilder);
        try {
            RestHighLevelClient client = getClient();
            countResponse = client.count(countRequest, RequestOptions.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
        }
        assert countResponse != null;
        return (int) countResponse.getCount();
    }

    public JSONArray searchDoc(String index, JSONObject searchJson, JSONObject rangeJson, int page, int pageSize, String[] excludeFields) {
        return searchDoc(index, searchJson,  page, pageSize, rangeJson,excludeFields,"_id",null,null,null);
    }

    /**
     * 根据查询条件在指定表中搜索数据
     *
     * @param index      索引
     * @param searchJson 查询条件
     * @param page       按照页数查询
     * @param pageSize   限制查询数量
     * @return 查询到的数据
     */
    public JSONArray searchDoc(String index, JSONObject searchJson,  int page, int pageSize, Object rangeObject,String[] excludeFields,String sortField,String sortString,JSONArray mustFieldArray,JSONArray withOutArray) {
        //构建搜索客户端
        SearchRequest searchRequest = new SearchRequest(index);
        //构建搜索请求
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //遍历查询条件
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        for (String keyString : searchJson.keySet()) {
            Object searchValue = searchJson.get(keyString);
            QueryBuilder queryBuilder = null;
            //如果搜索对象为String类型
            if(searchValue instanceof String ){
                queryBuilder=QueryBuilders.matchQuery(keyString, searchValue);
                qb = qb.must(queryBuilder);
            } else if (searchValue instanceof List<?>){
                for(Object one:(List<?>)searchValue){
                    queryBuilder=QueryBuilders.matchQuery(keyString, one);
                    qb = qb.should(queryBuilder);
                }
            }
            else{
                queryBuilder=QueryBuilders.matchQuery(keyString, searchValue);
                qb = qb.must(queryBuilder);
            }
        }
        if (excludeFields != null) {
            searchSourceBuilder.fetchSource(null, excludeFields);
        }
        if(mustFieldArray!=null){
            for (int i = 0; i < mustFieldArray.size(); i++) {
                String fileName = mustFieldArray.getString(i);
                QueryBuilder queryBuilder = QueryBuilders.existsQuery(fileName);
                qb.must(queryBuilder);
            }
        }
        if(withOutArray!=null){
            for (int i = 0; i < withOutArray.size(); i++) {
                String fileName = withOutArray.getString(i);
                QueryBuilder queryBuilder = QueryBuilders.existsQuery(fileName);
                qb.mustNot(queryBuilder);
            }
        }
        if(rangeObject instanceof JSONArray){
            JSONArray rangeArray = (JSONArray) rangeObject;
            for (int i = 0; i < rangeArray.size(); i++) {
                JSONObject rangeJson = rangeArray.getJSONObject(i);
                RangeQueryBuilder rangeQueryBuilder = getRange(rangeJson);
                if (rangeQueryBuilder != null) {
                    qb.must(rangeQueryBuilder);
                }
            }
        }
        if(rangeObject instanceof JSONObject){
            JSONObject rangeJson = (JSONObject) rangeObject;
            RangeQueryBuilder rangeQueryBuilder = getRange(rangeJson);
            if (rangeQueryBuilder != null) {
                qb.must(rangeQueryBuilder);
            }
        }
        searchSourceBuilder.query(qb);
        //如果存在分页参数，则开启分页
        if (page > 0 && pageSize > 0) {
            searchSourceBuilder.from((page - 1) * pageSize);
            searchSourceBuilder.size(pageSize);
        } else {
            searchSourceBuilder.from(0);
            searchSourceBuilder.size(10);
        }
        if(sortString!=null&&sortString.equals("ASC")){
            searchSourceBuilder.sort(sortField, SortOrder.ASC);
        } else if(sortString!=null&&sortString.equals("DESC")){
            searchSourceBuilder.sort(sortField, SortOrder.DESC);
        } else{
            searchSourceBuilder.sort(sortField);
        }

        searchRequest.source(searchSourceBuilder);
        JSONArray resultArray = new JSONArray();
        try {
            //提交搜索请求
            RestHighLevelClient client = getClient();
            RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
            builder.setHttpAsyncResponseConsumerFactory(new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(1048576000));
            SearchResponse searchResponse = client.search(searchRequest, builder.build());
//            SearchResponse searchResponse = client.search(searchRequest,RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            SearchHit[] searchHits = hits.getHits();
            //遍历返回的数据
            for (SearchHit hit : searchHits) {
                String hitString = hit.getSourceAsString();
                resultArray.add(JSON.parseObject(hitString));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultArray;
    }

    /**
     * 通过重载searchOneDoc方法，将分页参数默认为0
     */
    public JSONArray searchDoc(String index, JSONObject searchJson, int page, int pageSize) {
        return searchDoc(index, searchJson, new JSONObject(),page, pageSize, null);
    }

    /**
     * 根据ID获取到库中的指定记录
     *
     * @param index 索引
     * @param id    id
     * @return 返回此条记录的JSON格式的信息
     */
    public JSONObject getById(String index, String id) {
        GetRequest getRequest = new GetRequest(index, id);
        GetResponse getResponse = null;
        try {
            RestHighLevelClient client = getClient();
            getResponse = client.get(getRequest, RequestOptions.DEFAULT);
            return JSON.parseObject(getResponse.getSourceAsString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}