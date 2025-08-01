package kr.co.govengers.config;

//import kr.co.govengers.filter.JwtFilter; // ← jwt 필터 사용 시
import kr.co.govengers.config.CustomAuthenticationFilter; // ← custom 필터 사용 시
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    // private final JwtFilter jwtAuthenticationFilter;
    private final CustomAuthenticationFilter custFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/login",
                                "/api/join",
                                "/api/email/**",
                                "/api/sms/**",
                                "/api/products/**",
                                "/api/search/**",
                                "/api/notice/**",
                                "/api/review/**",
                                "/api/inquiry/**",
                                "/api/find-id",
                                "/api/find-id-by-email",
                                "/api/request-password-reset",
                                "/api/reset-password"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/email/send-code").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/uqna").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/uqna").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/wishlist/user").hasRole("USER")
                        .anyRequest().authenticated()
                )
              //.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                .addFilterBefore(custFilter, UsernamePasswordAuthenticationFilter.class);// 여기 세미콜론은 ok!

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
