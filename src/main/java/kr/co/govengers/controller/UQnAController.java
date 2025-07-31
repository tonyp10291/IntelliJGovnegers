package kr.co.govengers.controller;

import kr.co.govengers.entity.Inquiry;
import kr.co.govengers.entity.User;
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

    @PostMapping
    public ResponseEntity<Inquiry> createInquiry(@RequestBody Inquiry inquiry, @AuthenticationPrincipal User user) {
        Inquiry createdInquiry = uqnASvc.createInquiry(inquiry, user.getUid());
        return ResponseEntity.ok(createdInquiry);
    }
}