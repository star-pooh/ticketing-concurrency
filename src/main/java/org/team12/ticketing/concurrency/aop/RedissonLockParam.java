package org.team12.ticketing.concurrency.aop;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedissonLockParam {
}