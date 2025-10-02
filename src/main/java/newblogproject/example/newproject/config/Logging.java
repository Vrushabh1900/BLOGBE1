package newblogproject.example.newproject.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class Logging {
@Pointcut("within(newblogproject.example.newproject.service.BlogService)")
public void namedpointcut(){};

    @Around("namedpointcut()")
public Object aop(ProceedingJoinPoint joinPoint) throws Throwable {
      System.out.println("-------------------------------------------before method-------------------------------------------");
        Object jp= joinPoint.proceed();
        System.out.println("-----------------------------------after method-------------------------------------------");
        return jp;
    }

}
