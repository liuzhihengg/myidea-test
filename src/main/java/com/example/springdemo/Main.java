package com.example.springdemo;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        MyBean myBean = context.getBean(MyBean.class);
//        myBean.printMessage();
        A a = context.getBean(A.class);
        System.out.println(a.getB());

        context.getBean(MyService.class).doSomething();
//        a.getMyService().doSomething();
//        myService.myOwnMethod();
        context.close();
    }
}
