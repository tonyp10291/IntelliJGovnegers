package kr.co.govengers.controller;

import kr.co.govengers.service.SmsSvc;
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
public class SmsController {
    private final SmsSvc smsSvc;

    @PostMapping("/sms/send-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            smsSvc.sendVerificationCode(phone);
            return ResponseEntity.ok("인증번호가 발송되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("SMS 발송 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/sms/verify-code")
    public ResponseEntity<String> verifyCode(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String code = request.get("code");

        if (smsSvc.verifyCode(phone, code)) {
            return ResponseEntity.ok("인증에 성공했습니다.");
        } else {
            return ResponseEntity.status(400).body("인증번호가 올바르지 않습니다.");
        }
    }
}