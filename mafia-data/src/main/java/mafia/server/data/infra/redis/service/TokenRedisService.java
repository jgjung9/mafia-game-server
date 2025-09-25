package mafia.server.data.infra.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

@RequiredArgsConstructor
public class TokenRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final String KEY_STARTS_WITH = "token:";

    public void save(Long accountId, String token, long ttl) {
        redisTemplate.opsForValue()
                .set(getKey(accountId), token, Duration.ofSeconds(ttl));
    }

    public String get(Long accountId) {
        return redisTemplate.opsForValue()
                .get(getKey(accountId));
    }

    public void delete(Long accountId) {
        redisTemplate.opsForValue()
                .getAndDelete(getKey(accountId));
    }

    public String getKey(Long accountId) {
        return KEY_STARTS_WITH + accountId;
    }
}
