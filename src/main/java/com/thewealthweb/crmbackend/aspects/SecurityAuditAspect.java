package com.thewealthweb.crmbackend.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SecurityAuditAspect {

    @Before("execution(* com.thewealthweb..*Service.*(..)) && @annotation(org.springframework.security.access.prepost.PreAuthorize)")
    public void logSecurityCheck(JoinPoint joinPoint) {
        System.out.println("Security check: " + joinPoint.getSignature());
    }
}
