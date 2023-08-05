package com.maple.mp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 陈其丰
 */
@Getter
@AllArgsConstructor
public enum SexEnum{
    MALE(1, "男"),
    FEMALE(2,"女"),
    UNKNOWN(3,"未知");

    @EnumValue
    private final int value;
    private final String desc;

    @Override
    public String toString() {
        return this.desc;
    }
}
