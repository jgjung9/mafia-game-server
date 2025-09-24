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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(value = "spring.datasource.log.enabled", havingValue = "true")
@EnableJpaRepositories(
        basePackages = "mafia.server.data.domain.log",
        entityManagerFactoryRef = "logEntityManager",
        transactionManagerRef = "logTransactionManager"
)
public class LogDatabaseConfig {


    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.log")
    public DataSourceProperties logDatasourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource logDataSource(@Qualifier("logDatasourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean logEntityManager(
            EntityManagerFactoryBuilder builder,
            @Qualifier("logDataSource") DataSource dataSource
    ) {
        return builder.dataSource(dataSource)
                .packages("mafia.server.data.domain.log")
                .persistenceUnit("log")
                .build();
    }

    @Bean
    PlatformTransactionManager logTransactionManager(@Qualifier("logEntityManager") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public JPAQueryFactory logQueryFactory(@Qualifier("logEntityManager") EntityManager logEntityManager) {
        return new JPAQueryFactory(logEntityManager);
    }
}