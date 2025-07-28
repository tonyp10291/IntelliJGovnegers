package kr.com.GoGiProject.controller;

import kr.com.GoGiProject.entity.User; // User 임포트 추가
import kr.com.GoGiProject.service.UserSvc;
import kr.com.GoGiProject.util.JwtUtil; // JwtUtil 임포트 추가
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserSvc userSvc;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String uid = loginRequest.get("uid");
            String upw = loginRequest.get("upw");

            // UserSvc에서 사용자 인증
            User authenticatedUser = userSvc.login(uid, upw);

            // 인증 성공 시, JwtUtil을 사용하여 실제 토큰 생성
            String token = jwtUtil.generateToken(authenticatedUser);

            Map<String, String> response = Map.of("message", "로그인 성공", "token", token);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody User user) {
        try {
            userSvc.join(user);
            return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            // 이미 가입된 아이디인 경우 409 Conflict 응답을 보냄
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

}