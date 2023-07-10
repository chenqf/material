package com.maple.swagger.controller;

import com.maple.common.domain.Result;
import com.maple.swagger.pojo.User;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;


/**
 * @author 陈其丰
 */

@RequestMapping("/demo")
@RestController
@Api(value = "测试SwaggerController")
public class DemoController {

    @PostMapping("/test1")
    @ApiOperation(value = "测试方法1")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name",value = "姓名",required = true, paramType = "query"),
            @ApiImplicitParam(name = "age",value = "年龄",required = true, paramType = "query",dataType = "Integer")
    })
    public Result test1(String name, Integer age){
        return Result.success(name + "--" + age);
    }

    @PostMapping("/addUser")
    @ApiOperation(value = "添加用户")
    public Result test2(User user) {
        return Result.success(user);
    }

    @PostMapping("/getById/{id}")
    @ApiOperation(value = "通过id查询用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id",value = "编号",required = true, paramType = "path")
    })
    public Result<User> test3(@PathVariable("id") Integer id) {
        return Result.success(new User(id,"chenqf",20));
    }
}
