package mafia.server.web.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ExecutionTimerAspect {

    @Around("@annotation(mafia.server.web.common.annotation.ExecutionTime)")
    public Object doExecutionTimer(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;
        log.info("{} executionTime={}ms", joinPoint.getSignature(), executionTime);
        return result;
    }
}
