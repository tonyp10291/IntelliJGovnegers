package kr.co.govengers.controller;

import kr.co.govengers.entity.Inquiry;
import kr.co.govengers.entity.User;
import kr.co.govengers.service.UQnASvc;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/uqna")
@RequiredArgsConstructor
public class UQnAController {

    private final UQnASvc uqnASvc;

    @GetMapping
    public ResponseEntity<List<Inquiry>> getInquiries(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal User user
    ) {
        try {
            String userId = user != null ? user.getUid() : null;
            List<Inquiry> inquiries = uqnASvc.findInquiriesForUser(category, keyword, userId);
            return ResponseEntity.ok(inquiries);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createInquiry(
            @RequestBody Inquiry inquiry,
            @AuthenticationPrincipal User user
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (user == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            Inquiry createdInquiry = uqnASvc.createInquiry(inquiry, user.getUid());

            response.put("success", true);
            response.put("message", "등록되었습니다.");
            response.put("data", createdInquiry);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "등록 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * 문의 수정 (PUT)
     */
    @PutMapping("/{inquiryId}")
    public ResponseEntity<Map<String, Object>> updateInquiry(
            @PathVariable Long inquiryId,
            @RequestBody Inquiry inquiry,
            @AuthenticationPrincipal User user
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (user == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            Inquiry updatedInquiry = uqnASvc.updateInquiry(inquiryId, inquiry, user.getUid());

            response.put("success", true);
            response.put("message", "수정되었습니다.");
            response.put("data", updatedInquiry);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "수정 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * 문의 삭제 (DELETE)
     */
    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<Map<String, Object>> deleteInquiry(
            @PathVariable Long inquiryId,
            @AuthenticationPrincipal User user
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (user == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            uqnASvc.deleteInquiry(inquiryId, user.getUid());

            response.put("success", true);
            response.put("message", "삭제되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }
}