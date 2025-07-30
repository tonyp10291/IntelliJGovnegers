package kr.co.govengers.controller;

import kr.co.govengers.entity.User;
import kr.co.govengers.service.UserSvc;
import kr.co.govengers.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserSvc userSvc;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String uid = loginRequest.get("uid");
            String upw = loginRequest.get("upw");
            User authenticatedUser = userSvc.login(uid, upw);
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
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }

    @GetMapping("/test")
    public String testEndpoint() {
        return "UserController 테스트 성공!";
    }
}