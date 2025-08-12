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
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepo userRepo;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String lower = path == null ? "" : path.toLowerCase();

        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        if (lower.startsWith("/img/") || lower.startsWith("/images/") || lower.startsWith("/gogiimage/")
                || lower.startsWith("/css/") || lower.startsWith("/js/")
                || lower.startsWith("/static/") || lower.startsWith("/favicon") || lower.startsWith("/error")
                || lower.startsWith("/uploads/") || lower.startsWith("/api/imgs/") || lower.startsWith("/api/images/")
                || lower.startsWith("/api/download/") || lower.startsWith("/api/payment/")) {
            return true;
        }

        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".gif") || lower.endsWith(".webp")
                || lower.endsWith(".css") || lower.endsWith(".js") || lower.endsWith(".ico")) {
            return true;
        }

        if (lower.matches("^/api/products/\\d+/images/list$")) return true;               // POST
        if ("GET".equalsIgnoreCase(method) && lower.matches("^/api/products/\\d+/images$")) return true; // GET 폴백

        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        boolean isOpenGet =
                "GET".equalsIgnoreCase(method) &&
                        (path.startsWith("/api/products")
                                || path.startsWith("/api/reviews")
                                || path.startsWith("/api/qna")
                                || path.startsWith("/api/search")
                                || path.startsWith("/api/notices")
                                || path.startsWith("/api/inquiry"));

        boolean isOpenPost =
                "POST".equalsIgnoreCase(method) &&
                        ("/api/login".equals(path)
                                || "/api/join".equals(path)
                                || "/api/find-id".equals(path)
                                || "/api/find-id-by-email".equals(path)
                                || "/api/request-password-reset".equals(path)
                                || "/api/verify-user-for-password-reset".equals(path)
                                || "/api/reset-password".equals(path)
                                // 상세이미지 목록(공개)
                                || path.matches("/api/products/\\d+/images/list"));

        if (isOpenGet || isOpenPost) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String uid = jwtUtil.getUid(token);
        if (uid == null || uid.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<User> ou = userRepo.findByUid(uid);
        if (ou.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        User user = ou.get();
        String rawRole = user.getRole() == null ? "USER" : user.getRole();
        String mappedRole = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority(mappedRole))
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
