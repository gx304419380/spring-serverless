package com.fly.serverless;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * @author guoxiang
 */
@SpringBootApplication
@ServletComponentScan("com.fly.serverless.filter")
public class SpringServerlessApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringServerlessApplication.class, args);
    }

}
