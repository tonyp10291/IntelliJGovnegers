package kr.co.govengers.controller;

import kr.co.govengers.entity.User;
import kr.co.govengers.repository.UserRepo;
import kr.co.govengers.service.UserSvc;
import kr.co.govengers.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserSvc userSvc;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String uid = loginRequest.get("uid");
            String upw = loginRequest.get("upw");

            User authenticatedUser = userSvc.login(uid, upw);

            String token = jwtUtil.generateToken(authenticatedUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "로그인 성공");
            response.put("token", token);

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", authenticatedUser.getUid());
            userInfo.put("unm", authenticatedUser.getUnm());
            userInfo.put("umail", authenticatedUser.getUmail());
            userInfo.put("role", authenticatedUser.getRole());
            userInfo.put("point", authenticatedUser.getPoint());
            userInfo.put("emailVerified", authenticatedUser.isEmailVerified());
            userInfo.put("smsVerified", authenticatedUser.isSmsVerified());

            response.put("user", userInfo);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(401).body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "로그인 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/join")
    public ResponseEntity<Map<String, Object>> join(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        try {
            userSvc.join(user);

            response.put("success", true);
            response.put("message", "회원가입이 성공적으로 완료되었습니다.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(409).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "회원가입 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (user == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", user.getUid());
            userInfo.put("unm", user.getUnm());
            userInfo.put("umail", user.getUmail());
            userInfo.put("utel", user.getUtel());
            userInfo.put("ubt", user.getUbt());
            userInfo.put("role", user.getRole());
            userInfo.put("point", user.getPoint());
            userInfo.put("emailVerified", user.isEmailVerified());
            userInfo.put("smsVerified", user.isSmsVerified());

            response.put("success", true);
            response.put("user", userInfo);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "사용자 정보 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestBody Map<String, Object> updateRequest,
            @AuthenticationPrincipal User user) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (user == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            User updatedUser = userSvc.updateProfile(user.getUid(), updateRequest);

            response.put("success", true);
            response.put("message", "프로필이 성공적으로 수정되었습니다.");
            response.put("user", updatedUser);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "프로필 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "로그아웃되었습니다. 클라이언트에서 토큰을 삭제하세요.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();

        if (user != null) {
            response.put("success", true);
            response.put("message", "유효한 토큰입니다.");
            response.put("uid", user.getUid());
            response.put("role", user.getRole());
        } else {
            response.put("success", false);
            response.put("message", "유효하지 않은 토큰입니다.");
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/find-id")
    public ResponseEntity<Map<String, Object>> findId(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String unm = request.get("unm");
            String utel = request.get("utel");

            String uid = userSvc.findId(unm, utel);

            response.put("success", true);
            response.put("uid", uid);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "아이디 찾기 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/find-id-by-email")
    public ResponseEntity<Map<String, Object>> findIdByEmail(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String unm = request.get("unm");
            String umail = request.get("umail");

            String uid = userSvc.findIdByEmail(unm, umail);

            response.put("success", true);
            response.put("uid", uid);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "아이디 찾기 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/verify-user-for-password-reset")
    public ResponseEntity<Map<String, Object>> verifyUserForPasswordReset(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String uid = request.get("uid");
            String umail = request.get("umail");
            String utel = request.get("utel");

            User user = userSvc.getUserById(uid);

            if (umail != null && !user.getUmail().equals(umail)) {
                throw new IllegalArgumentException("아이디와 이메일이 일치하지 않습니다.");
            }

            if (utel != null && !user.getUtel().equals(utel)) {
                throw new IllegalArgumentException("아이디와 전화번호가 일치하지 않습니다.");
            }

            response.put("success", true);
            response.put("message", "사용자 확인이 완료되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String uid = request.get("uid");
            String newPassword = request.get("newPassword");

            User user = userSvc.getUserById(uid);
            user.setUpw(passwordEncoder.encode(newPassword));
            userRepo.save(user);

            response.put("success", true);
            response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "비밀번호 변경 중 오류가 발생했습니다.");
            return ResponseEntity.status(500).body(response);
        }
    }
}