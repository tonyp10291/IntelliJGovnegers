package kr.co.govengers.config;

import kr.co.govengers.filter.MypageCookieAuthFilter;
import kr.co.govengers.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class MypageSecurityConfig {

    private final UserRepo userRepo;

    @Bean
    public MypageCookieAuthFilter mypageCookieAuthFilter() {
        return new MypageCookieAuthFilter(userRepo);
    }

    @Bean
    @Order(0) // 이 체인을 우선 평가
    public SecurityFilterChain mypageFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/mypage/**")
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/mypage/set-uid").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(mypageCookieAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}