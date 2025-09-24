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
@ConditionalOnProperty(value = "spring.datasource.admin.enabled", havingValue = "true")
@EnableJpaRepositories(
        basePackages = "mafia.server.data.domain.admin",
        entityManagerFactoryRef = "adminEntityManager",
        transactionManagerRef = "adminTransactionManager"
)
public class AdminDatabaseConfig {


    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.admin")
    public DataSourceProperties adminDatasourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource adminDataSource(@Qualifier("adminDatasourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean adminEntityManager(
            EntityManagerFactoryBuilder builder,
            @Qualifier("adminDataSource") DataSource dataSource
    ) {
        return builder.dataSource(dataSource)
                .packages("mafia.server.data.domain.admin")
                .persistenceUnit("admin")
                .build();
    }

    @Bean
    PlatformTransactionManager adminTransactionManager(@Qualifier("adminEntityManager") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public JPAQueryFactory adminQueryFactory(@Qualifier("adminEntityManager") EntityManager adminEntityManager) {
        return new JPAQueryFactory(adminEntityManager);
    }
}