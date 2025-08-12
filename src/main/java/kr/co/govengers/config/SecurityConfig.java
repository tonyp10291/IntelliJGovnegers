package kr.co.govengers.config;

import kr.co.govengers.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;   // ✅ servlet용
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ filterChain은 "하나만"
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 정적/이미지 리소스 허용
                        .requestMatchers(
                                "/img/**", "/images/**", "/static/**", "/favicon.ico", "/error",
                                "/gogiImage/**", "/uploads/**", "/api/images/**", "/api/imgs/**"
                        ).permitAll()

                        // 상세이미지 목록은 공개
                        .requestMatchers(HttpMethod.POST, "/api/products/*/images/list").permitAll()
                        // 상세이미지 업로드는 관리자
                        .requestMatchers(HttpMethod.POST, "/api/products/*/images/upload").hasRole("ADMIN")

                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // (예시) 공개 API
                        .requestMatchers(HttpMethod.POST,
                                "/api/login", "/api/join",
                                "/api/find-id", "/api/find-id-by-email",
                                "/api/request-password-reset", "/api/verify-user-for-password-reset", "/api/reset-password"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/products/**",
                                "/api/notices/**",
                                "/api/qna/**",
                                "/api/reviews/**",
                                "/api/search/**",
                                "/api/inquiry/**"
                        ).permitAll()

                        // 나머지는 프로젝트 상황에 따라 결정
                        .anyRequest().permitAll()   // ⚠️ 개발 편의. 필요시 authenticated()로 변경
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:3000"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
