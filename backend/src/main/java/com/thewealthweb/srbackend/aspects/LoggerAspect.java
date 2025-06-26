package com.thewealthweb.srbackend.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

@Aspect
@Component
@Order(1)
public class LoggerAspect {

    private final Logger logger = Logger.getLogger(LoggerAspect.class.getName());

    @Around("execution(* com.thewealthweb..service..*(..))")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info(joinPoint.getSignature().toString() + " method execution start");
        Instant start = Instant.now();

        Object result = joinPoint.proceed();

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        logger.info("Time took to execute the method : " + timeElapsed);
        logger.info(joinPoint.getSignature().toString() + " method execution end");

        return result;
    }

    @Around("@annotation(com.thewealthweb.crmbackend.LogAspect)")
    public Object logWithAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info(joinPoint.toString() + " method execution start");
        Instant start = Instant.now();

        Object result = joinPoint.proceed();

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        logger.info("Time took to execute the method : " + timeElapsed);
        logger.info(joinPoint.getSignature().toString() + " method execution end");

        return result;
    }

    //@AfterThrowing(value = "execution(* com.thewealthweb.crmbackend.services.*.*(..))",throwing = "ex")
    @AfterThrowing(value = "execution(* com.thewealthweb..service..*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Exception ex) {
        logger.log(Level.SEVERE,joinPoint.getSignature()+ " An exception thrown with the help of" +
                " @AfterThrowing which happened due to : "+ex.getMessage());
    }

    //@AfterReturning(value = "execution(* com.thewealthweb.crmbackend.services.*.*(..))",returning = "retVal")
    @AfterReturning(value = "execution(* com.thewealthweb..service..*(..))", returning = "retVal")
    public void logStatus(JoinPoint joinPoint,Object retVal) {
        logger.log(Level.INFO,joinPoint.getSignature()+ " Method successfully processed with the status " +
                retVal.toString());
    }
}
