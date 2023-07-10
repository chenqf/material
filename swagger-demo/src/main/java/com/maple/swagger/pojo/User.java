package com.maple.swagger.pojo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 陈其丰
 */
@ApiModel("用户信息实体")
@Data
@AllArgsConstructor
public class User {
    @ApiModelProperty(value = "编号")
    private Integer id;
    @ApiModelProperty(value = "姓名",required = true)
    private String name;
    @ApiModelProperty(value = "年龄",required = true)
    private Integer age;
}
