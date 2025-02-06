package org.team12.ticketing.concurrency.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(0)
public class RedissonLockAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(redissonLock)")
    public Object around(ProceedingJoinPoint joinPoint, RedissonLock redissonLock) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs(); // 메서드의 실제 인자 값들
        Parameter[] parameters = method.getParameters(); // 메서드의 매개변수 정보

        // 락 키를 찾아 설정
        String lockKey = null;
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RedissonLockParam.class)) {
                lockKey = redissonLock.prefix() + args[i]; // prefix + 실제 파라미터 값
                break;
            }
        }

        if (lockKey == null) {
            throw new IllegalArgumentException("RedissonLock이 선언된 메서드에는 @RedissonLockParam이 있는 파라미터가 필요합니다.");
        }

        long threadId = Thread.currentThread().getId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            log.info("락 획득 시도: threadId = {}", threadId);
            if (lock.tryLock(10, 3, TimeUnit.SECONDS)) {
                try {
                    log.info("락 획득 성공: threadId = {}", threadId);
                    return joinPoint.proceed(); // 원래 메서드 실행
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        log.info("락 해제: threadId = {}", threadId);
                        lock.unlock();
                    }
                }
            } else {
                log.info("락 획득 실패: threadId = {}", threadId);
                throw new RuntimeException("현재 이용자가 많아 대기 중입니다. 잠시 후 다시 시도하세요.");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("예매 처리 중 오류 발생", e);
        }
    }
}