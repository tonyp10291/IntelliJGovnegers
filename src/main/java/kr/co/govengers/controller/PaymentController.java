package kr.co.govengers.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.govengers.service.PdOrdSvc;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import kr.co.govengers.service.OrderSvc;
import java.util.List;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/payment")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderSvc orderSvc;
    private final PdOrdSvc pdOrdSvc;

    private final Map<String, Map<String, Object>> payments = new ConcurrentHashMap<>();
    private final Map<String, String> impUidToMerchantUid = new ConcurrentHashMap<>();

    private static final String IMP_CODE = "imp26784823";
    private static final String API_KEY = "8541646403053004";
    private static final String API_SECRET = "syHpajKRTGFZuQI9530ZO7qJVR2f2JhR4rhJ17BJFfwuK6a7y02q7xQ38lXFv8C1Pt4kNtfB5PJRSfPy";
    private static final String PORTONE_API_URL = "https://api.iamport.kr";

    @PostMapping("/prepare")
    public ResponseEntity<Map<String, Object>> preparePayment(@RequestBody Map<String, Object> request) {
        try {
            log.info("=== 결제 준비 요청 시작 ===");
            log.info("요청 데이터: {}", request);

            String merchantUid = "ORDER_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);

            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("merchantUid", merchantUid);
            paymentData.put("amount", request.get("amount"));
            paymentData.put("productName", request.get("productName"));
            paymentData.put("buyerName", request.get("buyerName"));
            paymentData.put("buyerEmail", request.get("buyerEmail"));
            paymentData.put("buyerPhone", request.get("buyerPhone"));
            paymentData.put("status", "READY");
            paymentData.put("createdAt", System.currentTimeMillis());
            paymentData.put("deliveryInfo", request.get("deliveryInfo"));
            paymentData.put("payMethod", request.get("payMethod"));

            // ✅ 배송 정보와 상품 정보 추가
            paymentData.put("receiverName", request.get("receiverName"));
            paymentData.put("zipCode", request.get("zipCode"));
            paymentData.put("address", request.get("address"));
            paymentData.put("detailAddress", request.get("detailAddress"));
            paymentData.put("deliveryMemo", request.get("deliveryMemo"));

            // ✅ productInfo 배열에서 첫 번째 상품 정보 추출
            List<Map<String, Object>> productInfoList = (List<Map<String, Object>>) request.get("productInfo");
            if (productInfoList != null && !productInfoList.isEmpty()) {
                Map<String, Object> firstProduct = productInfoList.get(0);
                paymentData.put("productId", firstProduct.get("productId"));
                paymentData.put("productPrice", firstProduct.get("productPrice"));
                paymentData.put("quantity", firstProduct.get("quantity"));

                log.info("추출된 상품 정보 - productId: {}, quantity: {}",
                        firstProduct.get("productId"), firstProduct.get("quantity"));
                log.info("추출된 배송 정보 - receiverName: {}, zipCode: {}",
                        request.get("receiverName"), request.get("zipCode"));
            } else {
                log.warn("productInfo가 없거나 빈 배열입니다.");
            }

            paymentData.put("shippingCost", request.get("shippingCost"));
            paymentData.put("userId", request.get("userId"));
            paymentData.put("productInfoList", productInfoList);

            payments.put(merchantUid, paymentData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("merchantUid", merchantUid);
            response.put("amount", request.get("amount"));
            response.put("productName", request.get("productName"));
            response.put("buyerName", request.get("buyerName"));
            response.put("buyerEmail", request.get("buyerEmail"));
            response.put("buyerPhone", request.get("buyerPhone"));
            response.put("impCode", IMP_CODE);
            response.put("message", "결제 준비 완료");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("결제 준비 실패", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "결제 준비 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody Map<String, Object> request) {
        try {
            log.info("=== 결제 검증 요청 시작 ===");
            log.info("요청 데이터: {}", request);
            log.info("현재 메모리에 저장된 결제 목록: {}", payments.keySet());

            String impUid = (String) request.get("impUid");
            String merchantUid = (String) request.get("merchantUid");

            if (impUid == null || impUid.trim().isEmpty()) {
                throw new RuntimeException("impUid가 없습니다.");
            }

            log.info("포트원 API 호출 시작 - impUid: {}", impUid);
            String accessToken = getAccessToken();
            String url = PORTONE_API_URL + "/payments/" + impUid;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.get("code").asInt() != 0) {
                throw new RuntimeException("결제 정보 조회 실패: " + jsonNode.get("message").asText());
            }

            JsonNode paymentData = jsonNode.get("response");
            String portOneMerchantUid = paymentData.get("merchant_uid").asText();
            int paidAmount = paymentData.get("amount").asInt();
            String status = paymentData.get("status").asText();

            Map<String, Object> payment = payments.get(portOneMerchantUid);
            if (payment == null) {
                if (merchantUid != null && payments.containsKey(merchantUid)) {
                    payment = payments.get(merchantUid);
                    if (!merchantUid.equals(portOneMerchantUid)) {
                        log.warn("merchantUid 불일치 - 클라이언트:{}, 포트원:{}", merchantUid, portOneMerchantUid);
                    }
                } else {
                    throw new RuntimeException("주문을 찾을 수 없습니다: " + portOneMerchantUid);
                }
            }

            Object amountObj = payment.get("amount");
            int originalAmount =
                    (amountObj instanceof Integer i) ? i :
                            (amountObj instanceof Double d) ? d.intValue() :
                                    (amountObj instanceof String s) ? Integer.parseInt(s) :
                                            -1;

            if (originalAmount != paidAmount) {
                throw new RuntimeException("결제 금액 불일치. 원래:" + originalAmount + ", 실제:" + paidAmount);
            }

            boolean isSuccess = "paid".equals(status);
            payment.put("impUid", impUid);
            payment.put("status", isSuccess ? "COMPLETED" : "FAILED");
            payment.put("paidAt", isSuccess ? System.currentTimeMillis() : null);
            payment.put("portOneStatus", status);
            payment.put("verifiedAt", System.currentTimeMillis());

            impUidToMerchantUid.put(impUid, portOneMerchantUid);

            // 성공 시 DB 저장
            if (isSuccess) {
                try {
                    orderSvc.saveFromPaymentMap(payment, impUid);
                } catch (Exception ex) {
                    log.error("결제 정보 DB 저장 실패", ex);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", isSuccess);
            result.put("merchantUid", portOneMerchantUid);
            result.put("impUid", impUid);
            result.put("amount", paidAmount);
            result.put("status", payment.get("status"));
            result.put("message", isSuccess ? "결제가 완료되었습니다" : "결제가 실패했습니다");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("결제 검증 실패", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "결제 검증 실패: " + e.getMessage());
            error.put("error", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(@RequestBody Map<String, Object> webhook) {
        try {
            String impUid = (String) webhook.get("imp_uid");
            String merchantUid = (String) webhook.get("merchant_uid");
            String status = (String) webhook.get("status");

            if (impUid == null || merchantUid == null) {
                return ResponseEntity.ok(Map.of("status", "ignored"));
            }

            Map<String, Object> payment = payments.get(merchantUid);
            if (payment != null) {
                payment.put("impUid", impUid);
                payment.put("status", "paid".equals(status) ? "COMPLETED" : "FAILED");
                payment.put("portOneStatus", status);
                payment.put("webhookReceivedAt", System.currentTimeMillis());

                impUidToMerchantUid.put(impUid, merchantUid);
            } else {
                log.warn("웹훅 주문 미발견: {}", merchantUid);
            }

            return ResponseEntity.ok(Map.of("status", "success"));

        } catch (Exception e) {
            log.error("웹훅 처리 실패", e);
            return ResponseEntity.ok(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/debug/payments")
    public ResponseEntity<Map<String, Object>> getStoredPayments() {
        Map<String, Object> debug = new HashMap<>();
        debug.put("paymentsCount", payments.size());
        debug.put("impUidIndexCount", impUidToMerchantUid.size());
        debug.put("payments", payments);
        debug.put("impUidIndex", impUidToMerchantUid);
        return ResponseEntity.ok(debug);
    }

    @GetMapping("/status/imp/{impUid}")
    public ResponseEntity<Map<String, Object>> getPaymentStatusByImpUid(@PathVariable String impUid) {
        try {
            String merchantUid = impUidToMerchantUid.get(impUid);
            if (merchantUid == null) {
                throw new RuntimeException("해당 impUid로 주문을 찾을 수 없습니다: " + impUid);
            }

            Map<String, Object> payment = payments.get(merchantUid);
            if (payment == null) {
                throw new RuntimeException("주문 데이터를 찾을 수 없습니다: " + merchantUid);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("impUid", impUid);
            result.put("merchantUid", merchantUid);
            result.put("amount", payment.get("amount"));
            result.put("status", payment.get("status"));
            result.put("productName", payment.get("productName"));
            result.put("message", "조회 성공");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelPayment(@RequestBody Map<String, String> request) {
        try {
            String impUid = request.get("impUid");
            String accessToken = getAccessToken();
            String url = PORTONE_API_URL + "/payments/cancel";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);

            Map<String, Object> body = new HashMap<>();
            body.put("imp_uid", impUid);
            body.put("reason", "고객 요청");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            Map<String, Object> result = new HashMap<>();
            if (jsonNode.get("code").asInt() == 0) {
                String merchantUid = impUidToMerchantUid.get(impUid);
                if (merchantUid != null) {
                    Map<String, Object> payment = payments.get(merchantUid);
                    if (payment != null) {
                        payment.put("status", "CANCELLED");
                        payment.put("cancelledAt", System.currentTimeMillis());
                    }
                }

                result.put("success", true);
                result.put("message", "결제가 취소되었습니다");
            } else {
                result.put("success", false);
                result.put("message", "결제 취소 실패: " + jsonNode.get("message").asText());
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "결제 취소 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 관리자용 주문 전체 취소 - 수정 버전
    @PostMapping("/cancel/order")
    public ResponseEntity<Map<String, Object>> cancelOrder(@RequestBody Map<String, Object> request) {
        try {
            String reason = (String) request.getOrDefault("reason", "관리자 취소");
            Long orderId = Long.valueOf(request.get("orderId").toString());
            String impUid = (String) request.get("impUid"); // 프론트에서 받은 impUid

            log.info("관리자 주문 전체 취소 요청 - orderId: {}, impUid: {}", orderId, impUid);

            // ✅ 1. 포트원 결제 취소 먼저 처리
            if (impUid != null && !impUid.trim().isEmpty()) {
                log.info("포트원 결제 취소 진행 - impUid: {}", impUid);

                String accessToken = getAccessToken();
                String url = PORTONE_API_URL + "/payments/cancel";

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Authorization", "Bearer " + accessToken);

                Map<String, Object> body = new HashMap<>();
                body.put("imp_uid", impUid);
                body.put("reason", reason);

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                log.info("포트원 취소 요청 전송 - URL: {}, Body: {}", url, body);

                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

                log.info("포트원 응답 상태: {}", response.getStatusCode());
                log.info("포트원 응답 본문: {}", response.getBody());

                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                if (jsonNode.get("code").asInt() != 0) {
                    String errorMsg = jsonNode.get("message").asText();
                    log.error("포트원 취소 실패 - code: {}, message: {}",
                            jsonNode.get("code").asInt(), errorMsg);
                    throw new RuntimeException("포트원 결제 취소 실패: " + errorMsg);
                }

                log.info("포트원 결제 취소 성공!");

                // 메모리 상태 업데이트
                String merchantUid = impUidToMerchantUid.get(impUid);
                if (merchantUid != null) {
                    Map<String, Object> payment = payments.get(merchantUid);
                    if (payment != null) {
                        payment.put("status", "CANCELLED");
                        payment.put("cancelledAt", System.currentTimeMillis());
                    }
                }
            } else {
                log.warn("impUid가 없어서 포트원 취소를 건너뜁니다.");
            }

            // ✅ 2. DB 상태 변경
            pdOrdSvc.cancelWholeOrderAndRefund(orderId, reason);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "주문이 취소되었습니다");
            result.put("orderId", orderId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("관리자 주문 취소 실패", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "주문 취소 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 취소 가능 여부 확인
    @GetMapping("/cancel/check/{impUid}")
    public ResponseEntity<Map<String, Object>> checkCancelable(@PathVariable String impUid) {
        try {
            String accessToken = getAccessToken();
            String url = PORTONE_API_URL + "/payments/" + impUid;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            if (jsonNode.get("code").asInt() == 0) {
                JsonNode paymentData = jsonNode.get("response");
                String status = paymentData.get("status").asText();
                int paidAmount = paymentData.get("amount").asInt();
                int cancelledAmount = paymentData.get("cancel_amount") != null ?
                        paymentData.get("cancel_amount").asInt() : 0;

                boolean cancelable = "paid".equals(status) && (paidAmount > cancelledAmount);
                int remainingAmount = paidAmount - cancelledAmount;

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("cancelable", cancelable);
                result.put("paidAmount", paidAmount);
                result.put("cancelledAmount", cancelledAmount);
                result.put("remainingAmount", remainingAmount);
                result.put("status", status);

                return ResponseEntity.ok(result);
            } else {
                throw new RuntimeException("결제 정보 조회 실패: " + jsonNode.get("message").asText());
            }

        } catch (Exception e) {
            log.error("취소 가능 여부 확인 실패", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "취소 가능 여부 확인 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private String getAccessToken() {
        try {
            if (API_KEY == null || API_KEY.isBlank() || API_SECRET == null || API_SECRET.isBlank()) {
                throw new IllegalStateException("PortOne API_KEY/API_SECRET 미설정");
            }

            String url = PORTONE_API_URL + "/users/getToken";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            Map<String, String> body = Map.of(
                    "imp_key", API_KEY,
                    "imp_secret", API_SECRET
            );

            String json = objectMapper.writeValueAsString(body);
            log.debug("PortOne getToken 요청 바디: {}", json);

            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            if (jsonNode.get("code").asInt() == 0) {
                return jsonNode.get("response").get("access_token").asText();
            } else {
                throw new RuntimeException("토큰 발급 실패: " + jsonNode.get("message").asText());
            }
        } catch (Exception e) {
            log.error("액세스 토큰 발급 실패", e);
            throw new RuntimeException("액세스 토큰 발급 실패", e);
        }
    }
}