package com.msb.mall.mallsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.msb.mall.mallsearch.config.MallElasticSearchConfiguration;
import lombok.Data;
import lombok.ToString;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class MallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @Test
    void contextLoads() {
        System.out.println("--->"+client);
    }


    /**
     * 复杂的检索
     * 要实现商品数据的检索，我们需要把相关的信息存储到ES中
     * {
     *     skuId:9
     *     spuId:6  # 9 10 11 12
     *     skuTitle:小米
     *     price:5999
     *     saleCount:100
     *     attrs:[
     *      {机身内存:256G}
     *      {屏幕尺寸:85英寸}
     *      {CPU型号：晓龙xxx}
     *     ]
     * }
     * 100W * 20KB =2000W=1000000 * 20KB = 20GB
     * {
     *     skuId:9
     *     spuId:6  # 9 10 11 12
     *     skuTitle:小米
     *     price:5999
     *     saleCount:100
     * }
     *
     * {
     *     spuId:6
     *     attrs:[
     *      {机身内存:256G}
     *      {屏幕尺寸:85英寸}
     *      {CPU型号：晓龙xxx}
     *     ]
     * }
     *  华为：手机  家电  手环  自拍杆
     *  10000个 2000个spu
     *     2000个spu * 8 = 16KB
     *  10000个并发 * 16KB = 16G
     */
    @Test
    void searchIndexAll() throws IOException {
        // 1.创建一个 SearchRequest 对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank"); // 设置我们要检索的数据对应的索引库
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        /*sourceBuilder.query();
        sourceBuilder.from();
        sourceBuilder.size();
        sourceBuilder.aggregation();*/
        searchRequest.source(sourceBuilder);

        // 2.如何执行检索操作
        SearchResponse response = client.search(searchRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
        // 3.获取检索后的响应对象，我们需要解析出我们关心的数据
        System.out.println("ElasticSearch检索的信息："+response);
    }

    @Test
    void searchIndexByAddress() throws IOException {
        // 1.创建一个 SearchRequest 对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank"); // 设置我们要检索的数据对应的索引库
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 查询出bank下 address 中包含 mill的记录
        sourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        searchRequest.source(sourceBuilder);
        // System.out.println(searchRequest);

        // 2.如何执行检索操作
        SearchResponse response = client.search(searchRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
        // 3.获取检索后的响应对象，我们需要解析出我们关心的数据
        System.out.println("ElasticSearch检索的信息："+response);
    }


    @Test
    void searchIndexResponse() throws IOException {
        // 1.创建一个 SearchRequest 对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank"); // 设置我们要检索的数据对应的索引库
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 查询出bank下 address 中包含 mill的记录
        sourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        searchRequest.source(sourceBuilder);
        // System.out.println(searchRequest);

        // 2.如何执行检索操作
        SearchResponse response = client.search(searchRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
        // 3.获取检索后的响应对象，我们需要解析出我们关心的数据
       // System.out.println("ElasticSearch检索的信息："+response);
        RestStatus status = response.status();
        TimeValue took = response.getTook();
        SearchHits hits = response.getHits();
        TotalHits totalHits = hits.getTotalHits();
        TotalHits.Relation relation = totalHits.relation;
        long value = totalHits.value;
        float maxScore = hits.getMaxScore(); // 相关性的最高分
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit documentFields : hits1) {
            /*"_index" : "bank",
                    "_type" : "account",
                    "_id" : "970",
                    "_score" : 5.4032025*/
            //documentFields.getIndex(),documentFields.getType(),documentFields.getId(),documentFields.getScore();
            String json = documentFields.getSourceAsString();
            //System.out.println(json);
            // JSON字符串转换为 Object对象
            ObjectMapper mapper = new ObjectMapper();
            Account account = mapper.readValue(json, Account.class);
            System.out.println("account = " + account);
        }
        //System.out.println(relation.toString()+"--->" + value + "--->" + status);
    }

    @ToString
    @Data
    static class Account {

        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;

    }

    /**
     * 聚合：嵌套聚合
     * @throws IOException
     */
    @Test
    void searchIndexAggregation() throws IOException {
        // 1.创建一个 SearchRequest 对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank"); // 设置我们要检索的数据对应的索引库
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 查询出bank下 所有的文档
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        // 聚合 aggregation
        // 聚合bank下年龄的分布和每个年龄段的平均薪资
        AggregationBuilder aggregationBuiler = AggregationBuilders.terms("ageAgg")
                                                .field("age")
                                                .size(10);
        // 嵌套聚合
        aggregationBuiler.subAggregation(AggregationBuilders.avg("balanceAvg").field("balance"));

        sourceBuilder.aggregation(aggregationBuiler);
        sourceBuilder.size(0); // 聚合的时候就不用显示满足条件的文档内容了
        searchRequest.source(sourceBuilder);
        System.out.println(sourceBuilder);

        // 2.如何执行检索操作
        SearchResponse response = client.search(searchRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
        // 3.获取检索后的响应对象，我们需要解析出我们关心的数据
        System.out.println(response);
    }

    /**
     * 聚合
     * @throws IOException
     */
    @Test
    void searchIndexAggregation1() throws IOException {
        // 1.创建一个 SearchRequest 对象
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank"); // 设置我们要检索的数据对应的索引库
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 查询出bank下 所有的文档
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        // 聚合 aggregation
        // 聚合bank下年龄的分布和平均薪资
        AggregationBuilder aggregationBuiler = AggregationBuilders.terms("ageAgg")
                .field("age")
                .size(10);

        sourceBuilder.aggregation(aggregationBuiler);
        // 聚合平均年龄
        AvgAggregationBuilder balanceAggBuilder = AggregationBuilders.avg("balanceAgg").field("age");
        sourceBuilder.aggregation(balanceAggBuilder);

        sourceBuilder.size(0); // 聚合的时候就不用显示满足条件的文档内容了
        searchRequest.source(sourceBuilder);
        System.out.println(sourceBuilder);

        // 2.如何执行检索操作
        SearchResponse response = client.search(searchRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
        // 3.获取检索后的响应对象，我们需要解析出我们关心的数据
        System.out.println(response);
    }

    /**
     * 测试保存文档
     */
    @Test
    void saveIndex() throws Exception {
        IndexRequest indexRequest = new IndexRequest("system");
        indexRequest.id("1");
        // indexRequest.source("name","bobokaoya","age",18,"gender","男");
        User user = new User();
        user.setName("bobo");
        user.setAge(22);
        user.setGender("男");
        // 用Jackson中的对象转json数据
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(user);
        indexRequest.source(json, XContentType.JSON);
        // 执行操作
        IndexResponse index = client.index(indexRequest, MallElasticSearchConfiguration.COMMON_OPTIONS);
        // 提取有用的返回信息
        System.out.println(index);
    }
    @Data
    class User{
        private String name;
        private Integer age;
        private String gender;
    }

}
