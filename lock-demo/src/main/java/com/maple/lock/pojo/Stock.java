package com.maple.lock.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author 陈其丰
 */
@Data
@TableName("stock")
public class Stock {
    private Long id;
    private String productCode;
    private String warehouse;
    private Integer count;
    private Integer version;
}
