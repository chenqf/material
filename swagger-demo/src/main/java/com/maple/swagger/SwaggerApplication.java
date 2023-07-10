package com.maple.swagger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import springfox.documentation.oas.annotations.EnableOpenApi;


/**
 * @author chenqf
 */
@EnableOpenApi
@SpringBootApplication
public class SwaggerApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(SwaggerApplication.class, args);
    }
}
