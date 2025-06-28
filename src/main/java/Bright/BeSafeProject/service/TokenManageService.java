package Bright.BeSafeProject.service;

import Bright.BeSafeProject.vo.RedisPrefixEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenManageService {
    private final ReactiveStringRedisTemplate redisTemplate;

    public Mono<Void> saveRefreshToken(String refreshToken, String accountEmail){
        String redisKey = createKey(RedisPrefixEnum.REFRESH,accountEmail);
        return redisTemplate.opsForValue()
                .set(redisKey,refreshToken, Duration.ofDays(7))
                .then();
    }

    public Mono<String> getRefreshToken(String accountEmail){
        String redisKey = createKey(RedisPrefixEnum.REFRESH,accountEmail);
        return redisTemplate.opsForValue()
                .get(redisKey)
                .defaultIfEmpty("");
    }

    public Mono<Void> removeRefreshToken(String accountEmail){
        String redisKey = createKey(RedisPrefixEnum.REFRESH,accountEmail);
        return redisTemplate.delete(redisKey)
                .then();
    }

    public Mono<Void> saveBlacklistToken(String tokenString){
        String redisKey = createKey(RedisPrefixEnum.BLACKLIST,tokenString);
        return redisTemplate.opsForValue()
                .set(redisKey, String.valueOf(System.currentTimeMillis()), Duration.ofDays(1))
                .then();
    }

    public Mono<Boolean> checkTokenBlacklisted(String tokenString){
        String redisKey = createKey(RedisPrefixEnum.BLACKLIST,tokenString);
        return redisTemplate.hasKey(redisKey);
    }

    private String createKey(RedisPrefixEnum prefix, String tokenValue) {
        return String.join(":", prefix.getPrefix(), tokenValue);
    }
}
