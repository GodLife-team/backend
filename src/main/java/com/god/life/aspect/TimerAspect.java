package com.god.life.aspect;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class TimerAspect {


    @Around("@annotation(com.god.life.annotation.Timer)")
    public Object methodTimer(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long end = System.currentTimeMillis();
        log.info("{} 실행 시간 = {}ms", joinPoint.getSignature().getName(), end - start);
        return result;
    }

}
