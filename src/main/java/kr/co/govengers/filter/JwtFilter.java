package kr.co.govengers.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.govengers.entity.User;
import kr.co.govengers.repository.UserRepo;
import kr.co.govengers.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepo userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.equals("/api/join") || path.equals("/api/login")
                || path.equals("/api/email/send-code") || path.equals("/api/email/verify-code")
                || path.equals("/api/sms/send-code") || path.equals("/api/sms/verify-code")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String uid;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("=== JWT 필터: 토큰 없음 ===");
            System.out.println("요청 경로: " + path);
            System.out.println("Authorization 헤더: " + authHeader);
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        System.out.println("=== JWT 필터: 토큰 확인 ===");
        System.out.println("요청 경로: " + path);
        System.out.println("토큰: " + jwt.substring(0, Math.min(20, jwt.length())) + "...");

        if (jwtUtil.isTokenValid(jwt)) {
            System.out.println("토큰 유효성: 유효함");
            uid = jwtUtil.getUidFromToken(jwt);
            Optional<User> userOptional = userRepo.findById(uid);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                System.out.println("사용자 ID: " + user.getUid());
                System.out.println("사용자 권한: " + user.getRole());

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);

                System.out.println("설정된 Authentication: " + authToken);
                System.out.println("설정된 권한들: " + authToken.getAuthorities());
                System.out.println("인증 설정 완료!");
            } else {
                System.out.println("사용자를 찾을 수 없음: " + uid);
            }
        } else {
            System.out.println("토큰 유효성: 유효하지 않음");
        }
        System.out.println("========================");

        filterChain.doFilter(request, response);
    }
}