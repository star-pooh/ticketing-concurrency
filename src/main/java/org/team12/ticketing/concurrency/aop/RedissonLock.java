package org.team12.ticketing.concurrency.aop;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedissonLock {
    String prefix() default "concert_lock_"; // 기본 락 키 접두사
}
