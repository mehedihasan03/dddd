package net.celloscope.utils;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

public class RedisDataServiceUtils {

    private final RedisClient redisClient;

    public RedisDataServiceUtils(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public void saveDataWithExpiration(String key, String value, int expirationMinute) {
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> redisCommand = connection.sync();
        redisCommand.setex(key, expirationMinute, value);
        connection.close();
    }
}
