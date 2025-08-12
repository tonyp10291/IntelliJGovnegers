package kr.co.govengers.controller;

import kr.co.govengers.entity.enums.AdminStatus;
import kr.co.govengers.service.PdOrdSvc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/pdord")
@RequiredArgsConstructor
public class PdOrdController {

    private final PdOrdSvc pdOrdSvc;

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
            log.info("주문 목록 조회 요청 - page: {}, size: {}, keyword: {}", page, size, keyword);

            Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());

            LocalDateTime startDateTime = null;
            LocalDateTime endDateTime = null;

            if (startDate != null && !startDate.isBlank()) {
                try {
                    startDateTime = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException e) {
                    log.warn("시작 날짜 파싱 실패: {}", startDate, e);
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "시작 날짜 형식이 올바르지 않습니다: " + startDate
                    ));
                }
            }

            if (endDate != null && !endDate.isBlank()) {
                try {
                    endDateTime = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (DateTimeParseException e) {
                    log.warn("종료 날짜 파싱 실패: {}", endDate, e);
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "종료 날짜 형식이 올바르지 않습니다: " + endDate
                    ));
                }
            }

            AdminStatus status = null;
            if (orderStatus != null && !orderStatus.isBlank()) {
                status = toAdminStatusFlexible(orderStatus);
            }

            Page<Map<String, Object>> pageData = pdOrdSvc.getOrdersWithPaging(
                    pageable, keyword, status, paymentMethod, startDateTime, endDateTime);

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("content", pageData.getContent());
            res.put("totalPages", pageData.getTotalPages());
            res.put("totalElements", pageData.getTotalElements());
            res.put("size", pageData.getSize());
            res.put("number", pageData.getNumber());

            log.info("주문 목록 조회 성공 - 총 {}건", pageData.getTotalElements());
            return ResponseEntity.ok(res);

        } catch (IllegalArgumentException e) {
            log.error("잘못된 파라미터: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "잘못된 요청 파라미터입니다: " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("주문 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "주문 목록 조회 중 서버 오류가 발생했습니다. 관리자에게 문의하세요.",
                    "error", e.getMessage()
            ));
        }
    }

    /* ===== 통계 ===== */
    @GetMapping("/orders/statistics")
    public ResponseEntity<?> getOrderStatistics() {
        try {
            log.info("주문 통계 조회 요청");
            Map<String, Object> statistics = pdOrdSvc.getOrderStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.putAll(statistics);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("통계 정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "통계 정보를 가져오는데 실패했습니다. 관리자에게 문의하세요.",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId,
                                               @RequestBody Map<String, String> req) {
        try {
            log.info("주문 상태 변경 요청 - orderId: {}", orderId);

            if (orderId == null || orderId <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "올바르지 않은 주문 ID입니다."
                ));
            }

            String s = req.get("orderStatus");
            if (s == null || s.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "주문 상태는 필수입니다."
                ));
            }

            AdminStatus as = toAdminStatusFlexible(s);
            pdOrdSvc.updateOrderStatus(orderId, as);

            log.info("주문 상태 변경 완료 - orderId: {}, status: {}", orderId, as);
            return ResponseEntity.ok(Map.of("success", true, "message", "주문 상태가 변경되었습니다."));

        } catch (IllegalArgumentException e) {
            log.error("주문 상태 변경 - 잘못된 파라미터: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("주문 상태 변경 중 오류 발생 - orderId: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "주문 상태 변경 중 서버 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId,
                                         @RequestBody(required = false) Map<String,String> req) {
        try {
            log.info("주문 전체 취소 요청 - orderId: {}", orderId);

            if (orderId == null || orderId <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "올바르지 않은 주문 ID입니다."
                ));
            }

            String reason = (req != null) ? req.getOrDefault("cancelReason","관리자 취소") : "관리자 취소";
            pdOrdSvc.cancelWholeOrderAndRefund(orderId, reason); // ★ 포트원 전체취소 호출

            log.info("주문 전체 취소 완료 - orderId: {}, reason: {}", orderId, reason);
            return ResponseEntity.ok(Map.of("success", true, "message", "주문이 취소되었습니다."));

        } catch (IllegalArgumentException e) {
            log.error("주문 취소 - 잘못된 파라미터: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("주문 취소 중 오류 발생 - orderId: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "주문 취소 중 서버 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long orderId) {
        try {
            log.info("주문 상세 조회 요청 - orderId: {}", orderId);

            if (orderId == null || orderId <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "올바르지 않은 주문 ID입니다."
                ));
            }

            Map<String, Object> orderDetail = pdOrdSvc.getOrderDetailForAdmin(orderId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.putAll(orderDetail);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("주문 상세 조회 - 잘못된 파라미터: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("주문 상세 조회 중 오류 발생 - orderId: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "주문 상세 조회 중 서버 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/orders/bulk-status")
    public ResponseEntity<?> bulkUpdateOrderStatus(@RequestBody Map<String, Object> req) {
        try {
            log.info("일괄 상태 변경 요청");

            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) req.get("orderIds");
            String s = (String) req.get("orderStatus");

            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "선택된 주문이 없습니다."
                ));
            }

            if (s == null || s.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "변경할 주문 상태를 입력해주세요."
                ));
            }

            AdminStatus as = toAdminStatusFlexible(s);
            pdOrdSvc.bulkUpdateOrderStatus(ids, as);

            log.info("일괄 상태 변경 완료 - 대상: {}건, status: {}", ids.size(), as);
            return ResponseEntity.ok(Map.of("success", true, "message", "선택된 주문들의 상태가 변경되었습니다."));

        } catch (ClassCastException e) {
            log.error("일괄 상태 변경 - 잘못된 요청 데이터 형식", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "요청 데이터 형식이 올바르지 않습니다."
            ));
        } catch (Exception e) {
            log.error("일괄 상태 변경 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "일괄 상태 변경 중 서버 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    /* ===== 단건 취소(부분 환불) — 이거 하나만! ===== */
    @PutMapping("/order-items/{itemId}/cancel")
    public ResponseEntity<?> cancelOrderItem(@PathVariable Long itemId,
                                             @RequestBody(required = false) Map<String,String> req) {
        try {
            log.info("상품 단건 취소 요청 - itemId: {}", itemId);

            if (itemId == null || itemId <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "올바르지 않은 상품 ID입니다."
                ));
            }

            String reason = (req != null) ? req.getOrDefault("reason","관리자 단건 취소") : "관리자 단건 취소";
            pdOrdSvc.cancelOrderItemWithPayment(itemId, reason);

            log.info("상품 단건 취소 완료 - itemId: {}, reason: {}", itemId, reason);
            return ResponseEntity.ok(Map.of("success", true, "message", "상품이 취소(부분환불)되었습니다."));

        } catch (IllegalArgumentException e) {
            log.error("상품 취소 - 잘못된 파라미터: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("상품 취소 중 오류 발생 - itemId: {}", itemId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "상품 취소 중 서버 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    private AdminStatus toAdminStatusFlexible(String s) {
        if (s == null || s.isBlank()) {
            return AdminStatus.주문완료;
        }

        s = s.trim();

        switch (s) {
            case "주문완료": return AdminStatus.주문완료;
            case "결제완료": return AdminStatus.주문완료;      // 비즈 규칙
            case "배송준비": return AdminStatus.배송준비중;
            case "배송중":   return AdminStatus.배송중;
            case "배송완료": return AdminStatus.배송완료;
            case "취소완료": return AdminStatus.주문취소완료;
        }

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

        try {
            return AdminStatus.valueOf(s);
        } catch (IllegalArgumentException ignore) {
            log.warn("알 수 없는 주문 상태: {}, 기본값으로 설정", s);
        }

        return AdminStatus.주문완료;
    }
}