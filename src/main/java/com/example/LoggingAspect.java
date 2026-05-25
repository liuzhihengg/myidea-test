package com.example;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    // 这里的切点表达式配置为拦截com.example.service.MyService类中的所有方法
    @Before("execution(* com.example.springdemo.MyService.*(..))")
    public void logBeforeMethod(JoinPoint joinPoint) {
        System.out.println("Executing: " + joinPoint.getSignature().getName());
    }
}
