package com.maple.shiro.entity;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Serializable {
    @Serial
    private static final long serialVersionUID = -4008990799202866487L;
    private Long id;
    private String name;
    private String desc;
    private String realname;
}
