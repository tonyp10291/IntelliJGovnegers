package kr.co.govengers.config;

import kr.co.govengers.filter.JwtFilter;
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

    private final JwtFilter jwtFilter;

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
                        .requestMatchers("/img/**", "/css/**", "/js/**").permitAll()
                        .requestMatchers("/api/images/**").permitAll()
                        .requestMatchers("/api/imgs/**").permitAll()
                        .requestMatchers("/gogiImage/**").permitAll()
                        .requestMatchers("/api/download/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").authenticated()
                        .requestMatchers(
                                "/api/login",
                                "/api/join",
                                "/api/email/**",
                                "/api/sms/**",
                                "/api/products/**",
                                "/api/wishlist/guest/**",
                                "/api/search/**",
                                "/api/notices/**",
                                "/api/reviews/**",
                                "/api/inquiry/**",
                                "/api/find-id",
                                "/api/find-id-by-email",
                                "/api/request-password-reset",
                                "/api/verify-user-for-password-reset",
                                "/api/reset-password",
                                "/api/cart/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payment/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/payment/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/payment/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/payment/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/payment/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/uqna").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/uqna").authenticated()
                        .requestMatchers("/api/wishlist/user/**").hasRole("USER")
                        .requestMatchers("/api/wishlist/migrate").hasRole("USER")
                        .requestMatchers("/api/cart/user/**").hasRole("USER")
                        .requestMatchers("/api/cart/migrate").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:80",
                "http://127.0.0.1:80",
                "https://service.iamport.kr",
                "https://api.iamport.kr"
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        configuration.setAllowCredentials(true);
        configuration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}