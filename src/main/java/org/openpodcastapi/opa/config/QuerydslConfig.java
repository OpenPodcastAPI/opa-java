package org.openpodcastapi.opa.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/// Configuration for QueryDSL entity management
@Configuration
public class QuerydslConfig {

    private final EntityManager entityManager;

    /// All-args constructor
    ///
    /// @param entityManager the autowired entity manager
    public QuerydslConfig(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /// @return a JPAQueryFactory initialized with an entity manager
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
