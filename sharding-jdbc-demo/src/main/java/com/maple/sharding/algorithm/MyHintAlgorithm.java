package com.maple.sharding.algorithm;

import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.hint.HintShardingValue;

import java.util.Collection;

/**
 * @author 陈其丰
 */
public class MyHintAlgorithm implements HintShardingAlgorithm {
    @Override
    public Collection<String> doSharding(Collection collection, HintShardingValue hintShardingValue) {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public String getType() {
        return null;
    }
}
