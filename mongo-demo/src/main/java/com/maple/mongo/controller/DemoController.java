package com.maple.mongo.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author chenqf
 */
@RestController
@RequestMapping("/demo")
public class DemoController {



    @RequestMapping("/test1")
    public String test1() throws InterruptedException, IOException {

        return "test1";
    }
}
