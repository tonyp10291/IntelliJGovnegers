package kr.co.govengers.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.govengers.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.List;

/**
 * 마이페이지 전용 쿠키/헤더 인증 필터
 * - 오직 /api/mypage/** 에서만 동작
 * - 쿠키명: MYPAGE_UID (값은 uid), 대안 헤더: X-Mypage-Uid
 */
@RequiredArgsConstructor
public class MypageCookieAuthFilter extends OncePerRequestFilter {

    private final UserRepo userRepo;

    private static final String[] TARGET_PREFIXES = {
            "/api/mypage/me",
            "/api/mypage/set-uid"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        final String uri = req.getRequestURI();
        if (!matchesTarget(uri)) {
            chain.doFilter(req, res);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String uid = null;
            Cookie c = WebUtils.getCookie(req, "MYPAGE_UID");
            if (c != null) uid = c.getValue();
            if (uid == null || uid.isBlank()) uid = req.getHeader("X-Mypage-Uid");

            if (uid != null && !uid.isBlank()) {
                userRepo.findById(uid).ifPresent(user -> {
                    var auth = new UsernamePasswordAuthenticationToken(user, null, List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            }
        }

        chain.doFilter(req, res);
    }

    private boolean matchesTarget(String uri) {
        for (String p : TARGET_PREFIXES) if (uri.startsWith(p)) return true;
        return false;
    }
}