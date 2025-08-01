package kr.co.govengers.controller;

import kr.co.govengers.entity.User;
import kr.co.govengers.service.UserSvc;
import kr.co.govengers.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserSvc userSvc;
    private final JwtUtil jwtUtil;

    // ✅ 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String uid = loginRequest.get("uid");
            String upw = loginRequest.get("upw");

            User authenticatedUser = userSvc.login(uid, upw);
            String token = jwtUtil.generateToken(authenticatedUser);

            Map<String, String> response = Map.of("message", "로그인 성공", "token", token);

            // 🔍 권한 출력 로그 (개발 확인용)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            for (GrantedAuthority au : authentication.getAuthorities()) {
                System.out.println("/login auth_role: " + au.getAuthority());
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    // ✅ 회원가입
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody User user) {
        try {
            userSvc.join(user);
            return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    // ✅ 비밀번호 재설정
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        try {
            userSvc.resetPassword(token, newPassword);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("토큰이 유효하지 않거나 만료되었습니다.");
        }
    }
}
