package kr.co.govengers.controller;

import kr.co.govengers.entity.Inquiry;
import kr.co.govengers.service.MQnASvc;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class MQnAController {

    private final MQnASvc mqnaSvc;

    @GetMapping
    public Page<Inquiry> getInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String answerStatus,
            @RequestParam(required = false) Boolean isPrivate
    ) {
        Pageable pageable = PageRequest.of(page, 10);

        if (keyword != null && !keyword.isBlank()) {
            return mqnaSvc.searchInquiriesByKeyword(keyword, pageable);
        }

        if (category != null && !category.isBlank()) {
            return mqnaSvc.getInquiriesByCategory(category, pageable);
        }

        if (answerStatus != null && !answerStatus.isBlank()) {
            return mqnaSvc.getInquiriesByAnswerStatus(answerStatus, pageable);
        }

        if (isPrivate != null) {
            return mqnaSvc.getInquiriesByPrivacy(isPrivate, pageable);
        }

        return mqnaSvc.getPagedInquiries(pageable);
    }

    @GetMapping("/{inquiryId}")
    public ResponseEntity<Inquiry> getInquiryDetail(@PathVariable Long inquiryId) {
        try {
            Inquiry inquiry = mqnaSvc.findById(inquiryId);
            return ResponseEntity.ok(inquiry);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{inquiryId}/answer")
    public ResponseEntity<Map<String, Object>> addOrUpdateAnswer(
            @PathVariable Long inquiryId,
            @RequestBody Map<String, String> request
    ) {
        try {
            String answer = request.get("answer");
            String adminId = request.get("adminId");

            Inquiry updatedInquiry = mqnaSvc.addAnswer(inquiryId, answer, adminId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "답변이 등록되었습니다.");
            response.put("data", updatedInquiry);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<Map<String, Object>> deleteInquiry(@PathVariable Long inquiryId) {
        try {
            mqnaSvc.deleteInquiry(inquiryId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "문의가 삭제되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", mqnaSvc.getTotalCount());
        stats.put("pendingCount", mqnaSvc.getPendingCount());
        stats.put("answeredCount", mqnaSvc.getAnsweredCount());
        stats.put("privateCount", mqnaSvc.getPrivateCount());

        return ResponseEntity.ok(stats);
    }
}