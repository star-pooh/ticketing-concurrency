package org.team12.ticketing.concurrency.lock;

import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRockRepository {

    private final RedisCommands<String, String> redisCommands; //Bean 주입 필요


    public boolean acquireLock(String key, String value, long timeoutMillis) {
        String result = redisCommands.set(key, value, SetArgs.Builder.nx().px(timeoutMillis));
        return "OK".equals(result);
    }

    public void releaseLock(String key, String value) {
        if (value.equals(redisCommands.get(key))) {
            redisCommands.del(key);
        }
    }

    // 락 확인 메서드
    public boolean isLocked(String key) {
        return redisCommands.get(key) != null;
    }

}
