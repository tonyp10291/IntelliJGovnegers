package kr.com.GoGiProject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 비밀번호를 암호화하는 방식을 결정하는 Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 보안 규칙을 상세하게 설정하는 부분
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보안 기능 비활성화 (Stateless 서버에서는 불필요)
                .csrf(csrf -> csrf.disable())

                // 기본 제공 로그인 폼 비활성화 (우리가 직접 만들 것이기 때문)
                .formLogin(form -> form.disable())

                // 세션을 사용하지 않는 Stateless 서버로 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // URL 경로별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // "/login", "/join" 경로는 누구나 접근 허용
                        .requestMatchers("/login", "/join").permitAll()
                        // 그 외 모든 경로는 인증된 사용자만 접근 허용
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}