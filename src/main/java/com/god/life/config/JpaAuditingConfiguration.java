package com.god.life.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring 컨테이너를 요구하는 test는 가장 기본이 되는 Application 클래스가 항상 로드됨
 * Application 클래스에 @EnableJPaAuditing 어노테이션을 달아 모든 test들이 Jpa관련 bean들을 필요로 하는 상태가 됨
 * 통합 test는 전체 context를 로드하고 Jpa를 포함한 모든 Bean들을 주입받기 때문에 에러가 발생 X
 * 하지만 단위 테스트는 Jpa 관련 bean을 전혀 로드하지 않는 단위테스트이기 때문에 에러가 발생하는 것이다.
 */
@EnableJpaAuditing
@Configuration
public class JpaAuditingConfiguration {
}
