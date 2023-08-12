package com.maple.sharding.algorithm;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;


public class MyDbStandardAlgorithm implements StandardShardingAlgorithm {
    /**
     * 精确匹配时, 进入该方法
     * 返回对应的db
     */
    @Override
    public String doSharding(Collection collection, PreciseShardingValue preciseShardingValue) {
        // collection : {m1,m2}
        // preciseShardingValue : {logicTableName:course,columnName:id,value:查询的精确值}
        return "m1";
    }

    /**
     * 范围匹配, 进入该方法
     * 返回对应的多个db
     */
    @Override
    public Collection<String> doSharding(Collection collection, RangeShardingValue rangeShardingValue) {
        // collection : {m1,m2}
        // rangeShardingValue : {logicTableName:course,columnName:id,valueRange:查询的范围值}
        return collection;
    }

    @Override
    public void init() {

    }

    @Override
    public String getType() {
        return null;
    }
}
