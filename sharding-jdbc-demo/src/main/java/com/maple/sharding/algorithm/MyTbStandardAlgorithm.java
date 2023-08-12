package com.maple.sharding.algorithm;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import java.util.Collection;

/**
 * @author 陈其丰
 */
public class MyTbStandardAlgorithm implements StandardShardingAlgorithm {
    /**
     * 精确匹配时, 进入该方法
     * 返回对应的table
     */
    @Override
    public String doSharding(Collection collection, PreciseShardingValue preciseShardingValue) {
        // collection : {course_0,course_1,course_2}
        // preciseShardingValue : 查询的精确值
        return "course_0";
    }

    /**
     * 范围匹配, 进入该方法
     * 返回对应的多个table
     */
    @Override
    public Collection<String> doSharding(Collection collection, RangeShardingValue rangeShardingValue) {
        // collection : {course_0,course_1,course_2}
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
