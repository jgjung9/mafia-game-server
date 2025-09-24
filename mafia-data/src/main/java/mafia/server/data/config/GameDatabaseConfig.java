package mafia.server.data.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(value = "spring.datasource.game.enabled", havingValue = "true")
@EnableJpaRepositories(
        basePackages = "mafia.server.data.domain.game",
        entityManagerFactoryRef = "gameEntityManager",
        transactionManagerRef = "gameTransactionManager"
)
public class GameDatabaseConfig {


    @Primary
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.game")
    public DataSourceProperties gameDatasourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public DataSource gameDataSource(@Qualifier("gameDatasourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean gameEntityManager(
            EntityManagerFactoryBuilder builder,
            @Qualifier("gameDataSource") DataSource dataSource
    ) {
        return builder.dataSource(dataSource)
                .packages("mafia.server.data.domain.game")
                .persistenceUnit("game")
                .build();
    }

    @Primary
    @Bean
    PlatformTransactionManager gameTransactionManager(@Qualifier("gameEntityManager") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Primary
    @Bean
    public JPAQueryFactory gameQueryFactory(@Qualifier("gameEntityManager") EntityManager gameEntityManager) {
        return new JPAQueryFactory(gameEntityManager);
    }
}