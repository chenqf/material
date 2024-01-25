package com.maple.shiro.entity;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission implements Serializable {
    @Serial
    private static final long serialVersionUID = 8408838514735456104L;
    private Long id;
    private String name;
    private String info;
    private String desc;
}
