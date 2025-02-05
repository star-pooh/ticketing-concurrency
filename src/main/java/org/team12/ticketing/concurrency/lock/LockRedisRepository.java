package org.team12.ticketing.concurrency.lock;

import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LockRedisRepository {

    private final RedisCommands<String, String> redisCommands; //Bean 주입 필요


    public boolean acquireLock(String key, String value, long timeoutMillis) {
        String result = redisCommands.set(key, value, SetArgs.Builder.nx().px(timeoutMillis));
        return "OK".equals(result);
    }

    public void releaseLock(String key, String value) {
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then " +
                        "   return redis.call('del',KEYS[1])" +
                        "else " +
                        "   return 0" +
                        "end";
        redisCommands.eval(script, io.lettuce.core.ScriptOutputType.INTEGER, new String[]{key}, value);
    }
}