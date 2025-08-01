package kr.co.govengers.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.govengers.entity.User;
import kr.co.govengers.service.UserSvc;
import kr.co.govengers.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    @Lazy  //나중에 생성되므로 에러 방지위해 필요할 때 객체 생성
    UserSvc usvc;

    @Autowired
    @Lazy  //나중에 생성되므로 에러 방지위해 필요할 때 객체 생성
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            // 토큰 처리 로직 (예: 검증, 사용자 정보 추출 등)
            if (jwtUtil.validateToken(token)) {
                String uid = jwtUtil.getUserIdFromToken(token);
                UserDetails userDetails = usvc.checkUser(uid);
                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authRequest.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authRequest);

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                for(GrantedAuthority au : authentication.getAuthorities()){
                    System.out.println("auth_role333: " + au.getAuthority());
                }
            }
            System.out.println("Extracted Token: " + token);
        } else {
            // Authorization 헤더가 없거나 형식이 올바르지 않은 경우 처리
            System.out.println("토큰 미존재시");

            String[] urls={"/api/login", "/api/join", "/api/email/", "/api/sms/"};
            for(String url : urls){
                if(request.getRequestURI().contains(url)){
                    filterChain.doFilter(request, response);
                    return;
                }
            }
            try {
                // 요청 바디에서 JSON 데이터 읽기 (예: {"uid": "user1", "upw": "!p0o9i8u7"})
                User loginRequest = objectMapper.readValue(request.getInputStream(), User.class);

                String uid = loginRequest.getUid();
                String upw = loginRequest.getUpw();

                if (uid == null) {
                    uid = "";
                }
                if (upw == null) {
                    upw = "";
                }

                uid = uid.trim();
                upw = upw.trim();

                UserDetails userDetails = usvc.checkUser(uid, upw);
                UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authRequest.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authRequest);

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                for(GrantedAuthority au : authentication.getAuthorities()){
                    System.out.println("auth_role555: " + au.getAuthority());
                }
            } catch (IOException e) {
                throw new AuthenticationServiceException("Failed to parse authentication request body", e);
            }
        }
        filterChain.doFilter(request, response);
    }

}