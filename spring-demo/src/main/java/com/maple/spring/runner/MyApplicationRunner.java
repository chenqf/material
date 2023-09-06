package com.maple.spring.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 应用启动最后执行
 */
@Component
@Order(1)
public class MyApplicationRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 启动参数: --k1=v1 k2=v2
        System.out.println("ApplicationRunner:启动参数: --k1=v1 k2=v2");
        List<String> nonOptionArgs = args.getNonOptionArgs();
        for (String nonOptionArg : nonOptionArgs) {
            System.out.println(nonOptionArg);
        }
        System.out.println("------------------");
        String[] sourceArgs = args.getSourceArgs();
        for (String sourceArg : sourceArgs) {
            System.out.println(sourceArg);
        }
        System.out.println("------------------");
        Set<String> optionNames = args.getOptionNames();
        for (String optionName : optionNames) {
            List<String> optionValues = args.getOptionValues(optionName);
            System.out.println(optionName);
            System.out.println(optionValues.get(0));
        }
    }
}
