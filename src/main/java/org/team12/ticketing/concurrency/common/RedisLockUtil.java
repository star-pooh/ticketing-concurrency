package org.team12.ticketing.concurrency.common;

public class RedisLockUtil {
    public static final String CONCERT_TICKET_LOCK_KEY = "concert_ticket_lock";
    public static final long CONCERT_LOCK_TIMEOUT_MS = 30000;
}
