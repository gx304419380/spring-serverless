package com.fly.serverless.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author guoxiang
 * @version 1.0.0
 * @since 2021/6/18
 */
@ResponseBody
@RequestMapping("/my")
public class MyController {

    @GetMapping("/test/hello")
    public String hello() {
        return "hello world";
    }
}
