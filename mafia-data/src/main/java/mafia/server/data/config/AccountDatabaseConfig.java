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
@ConditionalOnProperty(value = "spring.datasource.account.enabled", havingValue = "true")
@EnableJpaRepositories(
        basePackages = "mafia.server.data.domain.account",
        entityManagerFactoryRef = "accountEntityManager",
        transactionManagerRef = "accountTransactionManager"
)
public class AccountDatabaseConfig {


    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.account")
    public DataSourceProperties accountDatasourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource accountDataSource(@Qualifier("accountDatasourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean accountEntityManager(
            EntityManagerFactoryBuilder builder,
            @Qualifier("accountDataSource") DataSource dataSource
    ) {
        return builder.dataSource(dataSource)
                .packages("mafia.server.data.domain.account")
                .persistenceUnit("account")
                .build();
    }

    @Bean
    PlatformTransactionManager accountTransactionManager(@Qualifier("accountEntityManager") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public JPAQueryFactory accountQueryFactory(@Qualifier("accountEntityManager") EntityManager accountEntityManager) {
        return new JPAQueryFactory(accountEntityManager);
    }
}