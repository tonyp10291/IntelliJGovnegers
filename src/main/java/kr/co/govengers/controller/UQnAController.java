package kr.co.govengers.controller;

import kr.co.govengers.entity.Inquiry;
import kr.co.govengers.entity.User; // User 임포트 추가
import kr.co.govengers.service.UQnASvc;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/uqna")
@RequiredArgsConstructor
public class UQnAController {

    private final UQnASvc uqnASvc;

    @GetMapping
    public ResponseEntity<List<Inquiry>> getAllInquiries() {
        List<Inquiry> inquiries = uqnASvc.findAllInquiries();
        return ResponseEntity.ok(inquiries);
    }

    // --- 이 부분을 수정합니다 ---
    // String userId 대신, User 객체를 직접 받습니다.
    @PostMapping
    public ResponseEntity<Inquiry> createInquiry(@RequestBody Inquiry inquiry, @AuthenticationPrincipal User user) {
        // User 객체에서 아이디를 꺼내서 서비스에 전달합니다.
        Inquiry createdInquiry = uqnASvc.createInquiry(inquiry, user.getUid());
        return ResponseEntity.ok(createdInquiry);
    }
}