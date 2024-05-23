package com.maple.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WaterSensor {
    /**
     * 水位传感器类型
     */
    private String id;
    /**
     * 水位传感器记录时间戳
     */
    private Long ts;
    /**
     * 水位记录
     */
    private Integer vc;
}
