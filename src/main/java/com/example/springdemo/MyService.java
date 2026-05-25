package com.example.springdemo;

import org.springframework.stereotype.Component;

interface ServiceA {
    void doSomething();
}

interface ServiceB {
    void doAnotherThing();
}

@Component
public class MyService implements ServiceA, ServiceB {
    public void doSomething() {
        // 实现ServiceAs
        System.out.println("皆苦的");
        this.myOwnMethod();
    }

    public void doAnotherThing() {
        // 实现ServiceB
    }

    public void myOwnMethod() {
        // MyService独有的方法
        System.out.println("我的");
    }
}
