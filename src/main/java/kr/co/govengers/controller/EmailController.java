package kr.co.govengers.controller;

import kr.co.govengers.service.EmailSvc;
import kr.co.govengers.service.UserSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EmailController {

    private final EmailSvc emailService;
    private final UserSvc userSvc;


    @PostMapping("/email/send-code")
    public ResponseEntity<String> sendVerificationEmail(@RequestBody Map<String, String> request) {
        System.out.println("✅ [EmailController] GET 인증 이메일 발송 진입: " + "umail");
        try {
            String email = request.get("umail");
            emailService.sendVerificationEmail(email);
            return ResponseEntity.ok("인증 이메일이 발송되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("이메일 발송 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/email/send-code")
    public ResponseEntity<String> sendVerificationEmailByGet(@RequestParam String umail) {
        try {
            emailService.sendVerificationEmail(umail);
            return ResponseEntity.ok("인증 이메일이 발송되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("이메일 발송 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/email/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody Map<String, String> request) {
        String email = request.get("umail");
        String code = request.get("code");

        if (emailService.verifyCode(email, code)) {
            return ResponseEntity.ok("이메일 인증에 성공했습니다.");
        } else {
            return ResponseEntity.status(400).body("인증번호가 올바르지 않습니다.");
        }
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<String> requestReset(@RequestBody Map<String, String> request) {
        String email = request.get("umail");
        try {
            userSvc.createPasswordResetTokenAndSendEmail(email);
            return ResponseEntity.ok("비밀번호 재설정 링크가 이메일로 전송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


}
