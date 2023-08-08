package net.celloscope;

import io.jsonwebtoken.Claims;
import io.lettuce.core.RedisClient;
import net.celloscope.utils.JwtServiceUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

@SpringBootApplication
public class MraAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MraAuthApplication.class, args);
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create("redis://127.0.0.1:6379"); // Should be replaced with Redis server URL
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
