package com.example.springdemo;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MyBean {
    public void printMessage(int a, int b, int c, Map<Integer, Integer> map, Map<Integer, Integer> m2,Map<Integer, Integer> m3) {System.out.println("Hello from MyBean!");
    }
}
