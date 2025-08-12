package kr.co.govengers.controller;

import kr.co.govengers.entity.enums.AdminStatus;
import kr.co.govengers.service.PdOrdSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/admin/pdord")
@RequiredArgsConstructor
public class PdOrdController {

    private final PdOrdSvc pdOrdSvc;

    /* ===== 주문 목록(페이징) ===== */
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());

            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;
            if (startDate != null && !startDate.isBlank()) {
                startDateTime = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            if (endDate != null && !endDate.isBlank()) {
                endDateTime = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }

            AdminStatus status = null;
            if (orderStatus != null && !orderStatus.isBlank()) {
                status = toAdminStatusFlexible(orderStatus);
            }

            Page<Map<String, Object>> pageData = pdOrdSvc.getOrdersWithPaging(
                    pageable, keyword, status, paymentMethod, startDateTime, endDateTime);

            Map<String, Object> res = new HashMap<>();
            res.put("content", pageData.getContent());
            res.put("totalPages", pageData.getTotalPages());
            res.put("totalElements", pageData.getTotalElements());
            res.put("size", pageData.getSize());
            res.put("number", pageData.getNumber());

            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "주문 목록 조회에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /* ===== 통계 ===== */
    @GetMapping("/orders/statistics")
    public ResponseEntity<?> getOrderStatistics() {
        try {
            return ResponseEntity.ok(pdOrdSvc.getOrderStatistics());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "통계 정보를 가져오는데 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /* ===== 주문 상태 변경 ===== */
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId,
                                               @RequestBody Map<String, String> req) {
        try {
            String s = req.get("orderStatus");
            AdminStatus as = toAdminStatusFlexible(s);
            pdOrdSvc.updateOrderStatus(orderId, as);
            return ResponseEntity.ok(Map.of("success", true, "message", "주문 상태가 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* ===== 주문 전체 취소(전체 환불 포함) ===== */
    @PutMapping("/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId,
                                         @RequestBody(required = false) Map<String,String> req) {
        try {
            String reason = (req != null) ? req.getOrDefault("cancelReason","관리자 취소") : "관리자 취소";
            pdOrdSvc.cancelWholeOrderAndRefund(orderId, reason); // ★ 포트원 전체취소 호출
            return ResponseEntity.ok(Map.of("success", true, "message", "주문이 취소되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* ===== 특정 주문 상세 ===== */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(pdOrdSvc.getOrderDetailForAdmin(orderId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* ===== 일괄 상태 변경 ===== */
    @PutMapping("/orders/bulk-status")
    public ResponseEntity<?> bulkUpdateOrderStatus(@RequestBody Map<String, Object> req) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) req.get("orderIds");
            String s = (String) req.get("orderStatus");
            AdminStatus as = toAdminStatusFlexible(s);
            pdOrdSvc.bulkUpdateOrderStatus(ids, as);
            return ResponseEntity.ok(Map.of("success", true, "message", "선택된 주문들의 상태가 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* ===== 단건 취소(부분 환불) — 이거 하나만! ===== */
    @PutMapping("/order-items/{itemId}/cancel")
    public ResponseEntity<?> cancelOrderItem(@PathVariable Long itemId,
                                             @RequestBody(required = false) Map<String,String> req) {
        try {
            String reason = (req != null) ? req.getOrDefault("reason","관리자 단건 취소") : "관리자 단건 취소";
            pdOrdSvc.cancelOrderItemWithPayment(itemId, reason);
            return ResponseEntity.ok(Map.of("success", true, "message", "상품이 취소(부분환불)되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /* ===== 상태 문자열 → AdminStatus (한국어/영문/enum 이름 모두 허용) ===== */
    private AdminStatus toAdminStatusFlexible(String s) {
        if (s == null) return AdminStatus.주문완료;
        s = s.trim();

        // 한국어
        switch (s) {
            case "주문완료": return AdminStatus.주문완료;
            case "결제완료": return AdminStatus.주문완료;      // 비즈 규칙
            case "배송준비": return AdminStatus.배송준비중;
            case "배송중":   return AdminStatus.배송중;
            case "배송완료": return AdminStatus.배송완료;
            case "취소완료": return AdminStatus.주문취소완료;
        }
        // 영문 코드
        switch (s.toUpperCase(Locale.ROOT)) {
            case "PENDING": return AdminStatus.주문완료;
            case "CONFIRMED": return AdminStatus.주문완료;
            case "PREPARING": return AdminStatus.배송준비중;
            case "SHIPPING": return AdminStatus.배송중;
            case "DELIVERED": return AdminStatus.배송완료;
            case "CANCELLED": return AdminStatus.주문취소완료;
            case "CANCEL_REQUESTED": return AdminStatus.주문취소요청;
            case "FAILED": return AdminStatus.주문실패;
        }
        // enum 이름 그대로 들어온 경우
        try { return AdminStatus.valueOf(s); } catch (Exception ignore) {}
        return AdminStatus.주문완료;
    }


}
