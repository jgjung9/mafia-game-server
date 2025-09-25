package mafia.server.data.config;

import mafia.server.data.infra.redis.service.TokenRedisService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@ConditionalOnProperty(value = "spring.data.redis.token.enabled", havingValue = "true")
public class TokenRedisConfig {

    @Bean
    @ConfigurationProperties(prefix = "redis.token")
    public RedisStandaloneConfiguration tokenRedisConfig() {
        return new RedisStandaloneConfiguration();
    }

    @Bean
    public RedisConnectionFactory tokenRedisConnectionFactory(
            @Qualifier("tokenRedisConfig") RedisStandaloneConfiguration config
    ) {
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, String> tokenRedisTemplate(
            @Qualifier("tokenRedisConnectionFactory") RedisConnectionFactory redisConnectionFactory
    ) {
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

    @Bean
    public TokenRedisService tokenRedisService(
            @Qualifier("tokenRedisTemplate") RedisTemplate<String, String> redisTemplate
    ) {
        return new TokenRedisService(redisTemplate);
    }
}
