package com.god.life.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Repository 단위 테스트를 위한 Configuration 설정
 */
@EnableJpaAuditing
@Configuration
public class JpaAuditingConfiguration {
}
