package com.god.life.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.god.life.error.handler.CustomAccessDeniedHandler;
import com.god.life.error.handler.CustomAuthenticationEntryPoint;
import com.god.life.service.JwtAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private final ObjectMapper objectMapper;
    private final AuthenticationManagerBuilder builder;

    public SecurityConfig(ObjectMapper objectMapper,
                          JwtAuthenticationProvider provider, AuthenticationManagerBuilder builder) {
        this.objectMapper = objectMapper;
        this.builder = builder;
        builder.authenticationProvider(provider);
    }

    @Bean
    public SecurityFilterChain chainConfig(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // CSRF 대비 X
                        .sessionManagement(sessionConfig -> { // 세선 사용 X
                            sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                        });

        http.authorizeHttpRequests(request -> {
            request.requestMatchers("/signup", "/check/**", "/fcm/**", "/reissue").permitAll();
            request.requestMatchers("/example/**").permitAll();
            //request.requestMatchers("/reissue").permitAll();
            request.requestMatchers("/admin").hasRole("ADMIN");
            request.anyRequest().authenticated();
        });

        http.formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        http.with(new JwtSecurityConfig(objectMapper, builder.getOrBuild()), Customizer.withDefaults());

        http.exceptionHandling(exceptionConfig -> {
            exceptionConfig.accessDeniedHandler(customAccessDeniedHandler())
                    .authenticationEntryPoint(customAuthenticationEntryPoint());
        });

        return http.build();
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return new CustomAccessDeniedHandler(objectMapper);
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint(){
        return new CustomAuthenticationEntryPoint(objectMapper);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return (web) ->
        {
            web.ignoring().requestMatchers("/api/**", "/v3/**");
        };
    }


}
