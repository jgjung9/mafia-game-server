package mafia.server.data.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConditionalOnProperty(value = "spring.data.redis.game.enabled", havingValue = "true")
public class GameRedisConfig {

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "redis.game")
    public RedisStandaloneConfiguration gameRedisConfig() {
        return new RedisStandaloneConfiguration();
    }

    @Bean
    @Primary
    public RedisConnectionFactory gameRedisConnectionFactory(
            RedisStandaloneConfiguration config
    ) {
        return new LettuceConnectionFactory(config);
    }

    @Bean
    @Primary
    public RedisTemplate<String, String> gameRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setDefaultSerializer(stringSerializer);
        template.setStringSerializer(stringSerializer);

        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);

        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        return template;
    }
}
