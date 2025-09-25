package mafia.server.data.infra.redis.service;

import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class TokenRedisServiceTest {

    @Autowired
    private TokenRedisService tokenRedisService;
    @Autowired
    @Qualifier("tokenRedisTemplate")
    private RedisTemplate<String, String> redisTemplate;

    @Container
    static RedisContainer redis = new RedisContainer("redis:7.4.5")
            .withExposedPorts(6380)
            .withReuse(true);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.token.host", redis::getHost);
        registry.add("spring.data.redis.token.port", () -> redis.getMappedPort(6380));
    }

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory()
                .getConnection()
                .serverCommands()
                .flushDb();
    }

    @Test
    @DisplayName("리프레시 토큰을 저장한다")
    void save() throws Exception {
        // given
        Long accountId = 1L;

        // when
        tokenRedisService.save(accountId, "testToken", 100);

        // then
        String token = redisTemplate.opsForValue().get(tokenRedisService.getKey(accountId));
        assertThat(token).isEqualTo("testToken");
    }

    @Test
    @DisplayName("리프레시 토큰을 가져온다")
    void get() throws Exception {
        // given
        Long accountId = 1L;

        // when
        redisTemplate.opsForValue()
                .set(tokenRedisService.getKey(accountId), "testToken", Duration.ofSeconds(100));

        // then
        String token = tokenRedisService.get(accountId);
        assertThat(token).isEqualTo("testToken");
    }

    @Test
    @DisplayName("리프레시 토큰을 삭제한다")
    void delete() throws Exception {
        // given
        Long accountId = 1L;
        redisTemplate.opsForValue()
                .set(tokenRedisService.getKey(accountId), "testToken", Duration.ofSeconds(100));

        // when
        tokenRedisService.delete(accountId);

        // then
        String token = redisTemplate.opsForValue()
                .get(tokenRedisService.getKey(accountId));
        assertThat(token).isNull();
    }
}