package com.maple.cache.controller;

import com.maple.cache.service.DemoService;
import com.maple.cache.service.EmployeeService;
import com.maple.common.domain.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 陈其丰
 */
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    @RequestMapping("/get")
    public Result get(){
        return Result.success(employeeService.getById(2));
    }

    @RequestMapping("/update")
    public Result update(){

        return Result.success(employeeService.updateById(2,"chenqf"));
    }

    @RequestMapping("/delete")
    public Result delete(){
        employeeService.deleteById(2);
        return Result.success("");
    }
}
