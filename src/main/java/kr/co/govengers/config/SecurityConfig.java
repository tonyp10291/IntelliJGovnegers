package kr.co.govengers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()  // ★ 모든 API 완전 오픈!
                )
                .formLogin(form -> form.disable())  // ★ 로그인 폼 자체도 disable!
                .httpBasic(httpBasic -> httpBasic.disable());
        return http.build();
    }
}
