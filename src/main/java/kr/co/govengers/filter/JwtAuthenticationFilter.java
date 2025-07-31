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
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepo userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String uid;

        // 1. Authorization 헤더가 없거나 "Bearer "로 시작하지 않으면 필터를 통과
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. "Bearer " 부분을 제외한 순수 토큰만 추출
        jwt = authHeader.substring(7);

        // 3. 토큰에서 사용자 아이디(uid)를 추출하고 유효성을 검증
        if (jwtUtil.isTokenValid(jwt)) {
            uid = jwtUtil.getUidFromToken(jwt);
            Optional<User> userOptional = userRepo.findById(uid);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // 4. Spring Security가 이해할 수 있는 형태로 인증 정보 생성
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
                );
                // 5. 생성된 인증 정보를 SecurityContext에 저장
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. 다음 필터로 요청을 전달
        filterChain.doFilter(request, response);
    }
}