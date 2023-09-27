package com.maple.mysql.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 陈其丰
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private Long id;
    private String name;
    private Double money;
}
