package com.maple.es.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maple.es.pojo.User;
import lombok.Cleanup;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;

import java.io.IOException;

/**
 * @author 陈其丰
 */
public class Test1 {




    public static void main(String[] args) throws IOException {
        // 创建ES客户端
        RestClientBuilder restClientBuilder = RestClient.builder(new HttpHost(System.getenv().get("ENV_CLOUD_IP"), 9200, "http"));
        @Cleanup RestHighLevelClient client = new RestHighLevelClient(restClientBuilder);

        // 创建索引
//        createIndex(client);
        // 查询索引
//        queryIndex(client);
        // 删除索引
//        deleteIndex(client);
        // 添加文档
//        createDoc(client);
        // 更新文档
//        upateDoc(client);
        // 查询数据
//        getDoc(client);
        // 删除数据
//        deleteDoc(client);
        // 批量插入
//        batchCreateDock(client);
        // 批量删除
//        batchDeleteDoc(client);
        // 查询索引中全部数据
//        queryAllDoc(client);
        // 分页条件查询
//        queryByCondition(client);
        // 组合查询
//        queryByCombine(client);
        // 范围查询
//        queryByRange(client);
        // 模糊查询
    }

    private static void queryByRange(RestHighLevelClient client) throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices("user");

        SearchSourceBuilder builder = new SearchSourceBuilder();
        RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("age");

        rangeQuery.gte(30);
        rangeQuery.lte(40);

        builder.query(rangeQuery);

        request.source(builder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        System.out.println(hits.getTotalHits()); // 总数
        System.out.println(response.getTook()); // 时间
        for (SearchHit hit : hits) {
            System.out.println(hit); // 每条记录
        }
    }

    private static void queryByCombine(RestHighLevelClient client) throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices("user");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        builder.query(boolQueryBuilder);
        boolQueryBuilder.must(QueryBuilders.matchQuery("name","zhangsan3")); // name必须为...
//        boolQueryBuilder.mustNot(QueryBuilders.matchQuery("name","zhangsan2")); // name必须不是...
//        boolQueryBuilder.should(QueryBuilders.matchQuery("name","zhangsan2")); // name可以是...

        request.source(builder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        System.out.println(hits.getTotalHits()); // 总数
        System.out.println(response.getTook()); // 时间
        for (SearchHit hit : hits) {
            System.out.println(hit); // 每条记录
        }
    }

    private static void queryByCondition(RestHighLevelClient client) throws IOException {
        SearchRequest request = new SearchRequest();
        request.indices("user");
        SearchSourceBuilder builder = new SearchSourceBuilder().query(QueryBuilders.termQuery("name", "zhangsan1"));
        builder.from(0); // 第1页
        builder.size(2); // 每页2条
        builder.sort("name", SortOrder.DESC); // 降序排列
        String[] includes = {}; // 包含字段
        String[] excludes = {}; // 排除字段
        builder.fetchSource(includes,excludes);
        request.source(builder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        System.out.println(hits.getTotalHits()); // 总数
        System.out.println(response.getTook()); // 时间
        for (SearchHit hit : hits) {
            System.out.println(hit); // 每条记录
        }
    }

    private static void queryAllDoc(RestHighLevelClient client) throws IOException {
                SearchRequest request = new SearchRequest();
        request.indices("user");
        request.source(new SearchSourceBuilder().query(QueryBuilders.matchAllQuery()));
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        System.out.println(hits.getTotalHits()); // 总数
        System.out.println(response.getTook()); // 时间
        for (SearchHit hit : hits) {
            System.out.println(hit); // 每条记录
        }
    }

    private static void batchDeleteDoc(RestHighLevelClient client) throws IOException {
                BulkRequest request = new BulkRequest();
        request.add(new DeleteRequest().index("user").id("1001"));
        request.add(new DeleteRequest().index("user").id("1002"));
        request.add(new DeleteRequest().index("user").id("1003"));
        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println(response.getTook());
    }

    private static void batchCreateDock(RestHighLevelClient client) throws IOException {
                BulkRequest request = new BulkRequest();
        request.add(new IndexRequest().index("user").id("1001").source(XContentType.JSON, "name", "zhangsan1"));
        request.add(new IndexRequest().index("user").id("1002").source(XContentType.JSON, "name", "zhangsan2"));
        request.add(new IndexRequest().index("user").id("1003").source(XContentType.JSON, "name", "zhangsan3"));
        request.add(new IndexRequest().index("user").id("1004").source(XContentType.JSON, "name", "zhangsan4"));
        request.add(new IndexRequest().index("user").id("1005").source(XContentType.JSON, "name", "zhangsan5"));
        request.add(new IndexRequest().index("user").id("1006").source(XContentType.JSON, "name", "zhangsan6"));
        request.add(new IndexRequest().index("user").id("1007").source(XContentType.JSON, "name", "zhangsan7"));
        BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println(response.getTook());
        System.out.println(response.getItems());
    }

    private static void deleteDoc(RestHighLevelClient client) throws IOException {
                DeleteRequest request = new DeleteRequest();
        request.index("user").id("1001");
        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(response.getResult());
    }

    private static void getDoc(RestHighLevelClient client) throws IOException {
        GetRequest request = new GetRequest();
        request.index("user").id("1001");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        System.out.println(response.getSourceAsString());
    }

    private static void upateDoc(RestHighLevelClient client) throws IOException {
        UpdateRequest request = new UpdateRequest();
        request.index("user").id("1001");
        request.doc(XContentType.JSON,"sex","女");
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
        System.out.println(response.getResult());
    }

    private static void createDoc(RestHighLevelClient client) throws IOException {
        IndexRequest request = new IndexRequest();
        request.index("user").id("1001");
        User user = new User();
        user.setName("chenqf");
        user.setAge(30);
        user.setSex("男");
        ObjectMapper mapper = new ObjectMapper();
        String userJson = mapper.writeValueAsString(user);
        request.source(userJson, XContentType.JSON);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);

        DocWriteResponse.Result result = response.getResult();
        System.out.println(result);
    }

    public void createIndex(RestHighLevelClient client) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("user");
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        boolean acknowledged = response.isAcknowledged();
        System.out.println("索引操作:" + acknowledged);
    }

    public void queryIndex(RestHighLevelClient client) throws IOException {
        GetIndexRequest request = new GetIndexRequest("user");
        GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);
        System.out.println(response.getAliases());
        System.out.println(response.getMappings());
    }

    private static void deleteIndex(RestHighLevelClient client) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("user");
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println("索引操作:" + response.isAcknowledged());
    }
}
