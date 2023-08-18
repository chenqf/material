package com.maple.kafka.kafka;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;

/**
 * @author 陈其丰
 */
public class MyPartitioner implements Partitioner {
    // 指定哪个Partition
    @Override
    public int partition(String topicName, Object messageKey, byte[] bytes, Object messageValue, byte[] bytes1, Cluster cluster) {
        int i = messageKey.hashCode();
        // 当前topic的所有Partition
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topicName);
        int numPartitions = partitions.size();
        return Math.abs(i % numPartitions);
    }

    @Override
    public void close() {

    }
    // 整理配置项
    @Override
    public void configure(Map<String, ?> map) {
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            System.out.println("entry.key:" + entry.getKey() + " === entry.value:" + entry.getValue());
        }
    }
}
