package com.example.springdemo;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Component
public class A {

    @Resource
    private List<B> bb;

    @Autowired
    public ServiceA myService;

    public List<B> getB() {
        return bb;
    }

    public ServiceA getMyService() {
        return myService;
    }

    @Bean
    public List<B> bb() {
        return Arrays.asList(new B());
    }

    @Bean
    public B bbb() {
        return new B();
    }
}
