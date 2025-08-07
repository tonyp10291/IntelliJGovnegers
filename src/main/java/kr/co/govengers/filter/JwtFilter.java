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
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.startsWith("/img/") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.startsWith("/api/imgs/") ||
                path.startsWith("/api/images/") ||
                path.startsWith("/gogiImage/") ||
                path.startsWith("/api/download/") ||
                path.startsWith("/api/payment/") ||
                path.endsWith(".jpg") ||
                path.endsWith(".jpeg") ||
                path.endsWith(".png") ||
                path.endsWith(".gif") ||
                path.endsWith(".webp") ||
                path.endsWith(".css") ||
                path.endsWith(".js");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 인증이 필요 없는 경로들
        if (path.equals("/api/join") || path.equals("/api/login")
                || path.equals("/api/email/send-code") || path.equals("/api/email/verify-code")
                || path.equals("/api/sms/send-code") || path.equals("/api/sms/verify-code")
                || path.startsWith("/api/payment/")
                || path.startsWith("/api/products/")
                || path.startsWith("/api/wishlist/guest/")
                || path.startsWith("/api/cart/")
                || path.startsWith("/api/search/")
                || path.startsWith("/api/notices/")
                || path.startsWith("/api/reviews/")
                || path.startsWith("/api/inquiry/")
                || path.equals("/api/find-id")
                || path.equals("/api/find-id-by-email")
                || path.equals("/api/request-password-reset")
                || path.equals("/api/verify-user-for-password-reset")
                || path.equals("/api/reset-password")
                || (path.equals("/api/uqna") && request.getMethod().equals("GET"))) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String uid;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        if (jwtUtil.isTokenValid(jwt)) {
            uid = jwtUtil.getUidFromToken(jwt);
            Optional<User> userOptional = userRepo.findById(uid);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}