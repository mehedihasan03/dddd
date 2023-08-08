package net.celloscope.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.login.domain.RedisTempData;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisUtils {

    private final ObjectMapper objectMapper;
    private final RedisClient redisClient;
    private final JwtServiceUtils jwtServiceUtils;

    public RedisUtils(ObjectMapper objectMapper, RedisClient redisClient, JwtServiceUtils jwtServiceUtils) {
        this.objectMapper = objectMapper;
        this.redisClient = redisClient;
        this.jwtServiceUtils = jwtServiceUtils;
    }

    public void saveRedisData(RedisTempData data) {
        try {
            String jsonValue = objectMapper.writeValueAsString(data);
            StatefulRedisConnection<String, String> connection = redisClient.connect();
            RedisCommands<String, String> redisCommands = connection.sync();
            redisCommands.setex(data.getUserOid(), 1800, jsonValue);
            log.info("saved Redis data is : {}", jsonValue);
            connection.close();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String deleteRedisData(String token) {
        Claims claims = jwtServiceUtils.decodeToken(token);
        StatefulRedisConnection<String, String> connection = redisClient.connect();
        RedisCommands<String, String> redisCommands = connection.sync();
        String redisValue = redisCommands.get(claims.get("userOid", String.class));
        if (redisValue != null && !redisValue.isEmpty()) {
            log.info("Found Redis data from Redis : {} ", redisValue);
            redisCommands.del(claims.get("userOid", String.class));
            log.info("Data Deleted from Redis.");
            connection.close();
            return "User log out successfully";
        } else {
            log.error("No Redis Data found in Redis");
            connection.close();
            return "Your is expire";
        }
    }

    public RedisTempData checkRedisData(String token) {
        Claims claims = jwtServiceUtils.decodeToken(token);
        if (jwtServiceUtils.isTokenExpired(claims)){
            throw new RuntimeException("Token is Expire");
        } else {
            StatefulRedisConnection<String, String> connection = redisClient.connect();
            RedisCommands<String, String> redisCommands = connection.sync();
            String redisValue = redisCommands.get(claims.get("userOid", String.class));
            connection.close();
            if (redisValue != null && !redisValue.isEmpty()) {
                log.info("Found Redis String Value : {} ", redisValue);
                return parseRedisDataTemp(redisValue);
            } else {
                log.error("Error form getting Redis Data");
                throw new NullPointerException("No Redis data found");
            }
        }
    }

    private RedisTempData parseRedisDataTemp(String redisValue) {
        try {
            return objectMapper.readValue(redisValue, RedisTempData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
