//추가
package kr.co.govengers.controller;

import kr.co.govengers.service.EmailSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EmailController {

    private final EmailSvc emailService;

    @PostMapping("/email/send-code")
    public ResponseEntity<String> sendVerificationEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            emailService.sendVerificationEmail(email);
            return ResponseEntity.ok("인증 이메일이 발송되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("이메일 발송 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/email/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (emailService.verifyCode(email, code)) {
            return ResponseEntity.ok("이메일 인증에 성공했습니다.");
        } else {
            return ResponseEntity.status(400).body("인증번호가 올바르지 않습니다.");
        }
    }
}