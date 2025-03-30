package com.auth.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.UUID;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("within(@org.springframework.web.bind.annotation.RestController *) || " +
            "within(@org.springframework.stereotype.Service *)")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        try {
            if (RequestContextHolder.getRequestAttributes() != null) {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                log.info("Request - Method: {} {} | Class: {} | Method: {}",
                        request.getMethod(), request.getRequestURI(), className, methodName);
            }

            log.info("Entering {} | Method: {} | Args: {}",
                    className, methodName, Arrays.toString(joinPoint.getArgs()));

            long start = System.currentTimeMillis();
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;

            log.info("Exiting {} | Method: {} | Duration: {}ms | Response: {}",
                    className, methodName, executionTime, result);

            return result;
        } catch (Exception e) {
            log.error("Exception in {} | Method: {} | Error: {}",
                    className, methodName, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}