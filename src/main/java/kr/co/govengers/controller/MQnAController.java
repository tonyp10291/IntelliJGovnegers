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

    // 전체 문의 목록 조회 (관리자용)
    @GetMapping
    public Page<Inquiry> getInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String answerStatus,
            @RequestParam(required = false) Boolean isPrivate
    ) {
        Pageable pageable = PageRequest.of(page, 10);

        // 검색어가 있으면 검색
        if (keyword != null && !keyword.isBlank()) {
            return mqnaSvc.searchInquiriesByKeyword(keyword, pageable);
        }

        // 카테고리 필터가 있으면 카테고리별 조회
        if (category != null && !category.isBlank()) {
            return mqnaSvc.getInquiriesByCategory(category, pageable);
        }

        // 답변 상태 필터가 있으면 상태별 조회
        if (answerStatus != null && !answerStatus.isBlank()) {
            return mqnaSvc.getInquiriesByAnswerStatus(answerStatus, pageable);
        }

        // 공개/비공개 필터가 있으면
        if (isPrivate != null) {
            return mqnaSvc.getInquiriesByPrivacy(isPrivate, pageable);
        }

        // 기본: 전체 조회 (최신순)
        return mqnaSvc.getPagedInquiries(pageable);
    }

    // 개별 문의 상세 조회
    @GetMapping("/{inquiryId}")
    public ResponseEntity<Inquiry> getInquiryDetail(@PathVariable Long inquiryId) {
        try {
            Inquiry inquiry = mqnaSvc.findById(inquiryId);
            return ResponseEntity.ok(inquiry);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 관리자 답변 등록/수정
    @PostMapping("/{inquiryId}/answer")
    public ResponseEntity<Map<String, Object>> addOrUpdateAnswer(
            @PathVariable Long inquiryId,
            @RequestBody Map<String, String> request
    ) {
        try {
            String answer = request.get("answer");
            String adminId = request.get("adminId"); // JWT에서 추출한 관리자 ID

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

    // 문의 삭제 (관리자 권한)
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

    // 대시보드용 통계 정보
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