package kr.co.govengers.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

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

            payments.put(merchantUid, paymentData);

            log.info("결제 데이터 저장 완료 - merchantUid: {}", merchantUid);
            log.info("현재 저장된 결제 건수: {}", payments.size());

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

            log.info("포트원 API 응답 상태: {}", response.getStatusCode());
            log.info("포트원 API 응답 본문: {}", response.getBody());

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            if (jsonNode.get("code").asInt() != 0) {
                throw new RuntimeException("결제 정보 조회 실패: " + jsonNode.get("message").asText());
            }

            JsonNode paymentData = jsonNode.get("response");
            String portOneMerchantUid = paymentData.get("merchant_uid").asText();
            int paidAmount = paymentData.get("amount").asInt();
            String status = paymentData.get("status").asText();

            log.info("포트원에서 조회한 결제 정보:");
            log.info("- merchantUid: {}", portOneMerchantUid);
            log.info("- amount: {}", paidAmount);
            log.info("- status: {}", status);

            Map<String, Object> payment = payments.get(portOneMerchantUid);
            if (payment == null) {
                log.error("주문을 찾을 수 없습니다.");
                log.error("포트원 merchantUid: {}", portOneMerchantUid);
                log.error("클라이언트에서 전송한 merchantUid: {}", merchantUid);
                log.error("메모리에 저장된 주문 목록: {}", payments.keySet());

                if (merchantUid != null && payments.containsKey(merchantUid)) {
                    log.info("클라이언트 merchantUid로 주문 발견: {}", merchantUid);
                    payment = payments.get(merchantUid);
                    if (!merchantUid.equals(portOneMerchantUid)) {
                        log.warn("merchantUid 불일치 감지 - 클라이언트: {}, 포트원: {}", merchantUid, portOneMerchantUid);
                    }
                } else {
                    throw new RuntimeException("주문을 찾을 수 없습니다: " + portOneMerchantUid);
                }
            }

            Object amountObj = payment.get("amount");
            int originalAmount;

            if (amountObj instanceof Integer) {
                originalAmount = (Integer) amountObj;
            } else if (amountObj instanceof Double) {
                originalAmount = ((Double) amountObj).intValue();
            } else if (amountObj instanceof String) {
                originalAmount = Integer.parseInt((String) amountObj);
            } else {
                throw new RuntimeException("결제 금액 형식이 올바르지 않습니다: " + amountObj);
            }

            log.info("금액 검증 - 원래 금액: {}, 실제 결제 금액: {}", originalAmount, paidAmount);

            if (originalAmount != paidAmount) {
                log.error("금액 불일치!");
                throw new RuntimeException("결제 금액이 일치하지 않습니다. 원래: " + originalAmount + ", 실제: " + paidAmount);
            }

            boolean isSuccess = "paid".equals(status);
            payment.put("impUid", impUid);
            payment.put("status", isSuccess ? "COMPLETED" : "FAILED");
            payment.put("paidAt", isSuccess ? System.currentTimeMillis() : null);
            payment.put("portOneStatus", status);
            payment.put("verifiedAt", System.currentTimeMillis());

            impUidToMerchantUid.put(impUid, portOneMerchantUid);

            log.info("결제 상태 업데이트 완료:");
            log.info("- 결제 상태: {}", payment.get("status"));
            log.info("- 포트원 상태: {}", status);

            Map<String, Object> result = new HashMap<>();
            result.put("success", isSuccess);
            result.put("merchantUid", portOneMerchantUid);
            result.put("impUid", impUid);
            result.put("amount", paidAmount);
            result.put("status", payment.get("status"));
            result.put("message", isSuccess ? "결제가 완료되었습니다" : "결제가 실패했습니다");

            log.info("=== 결제 검증 완료 ===");
            log.info("검증 결과: {}", result);

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
            log.info("=== 웹훅 수신 ===");
            log.info("웹훅 데이터: {}", webhook);

            String impUid = (String) webhook.get("imp_uid");
            String merchantUid = (String) webhook.get("merchant_uid");
            String status = (String) webhook.get("status");

            if (impUid == null || merchantUid == null) {
                log.warn("웹훅 데이터 불완전: impUid={}, merchantUid={}", impUid, merchantUid);
                return ResponseEntity.ok(Map.of("status", "ignored"));
            }

            Map<String, Object> payment = payments.get(merchantUid);
            if (payment != null) {
                payment.put("impUid", impUid);
                payment.put("status", "paid".equals(status) ? "COMPLETED" : "FAILED");
                payment.put("portOneStatus", status);
                payment.put("webhookReceivedAt", System.currentTimeMillis());

                impUidToMerchantUid.put(impUid, merchantUid);

                log.info("웹훅으로 결제 상태 업데이트 완료: {} -> {}", merchantUid, status);
            } else {
                log.warn("웹훅으로 전달받은 주문을 메모리에서 찾을 수 없음: {}", merchantUid);
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
            log.info("impUid로 결제 상태 조회: {}", impUid);

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
            log.error("impUid로 결제 상태 조회 실패", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelPayment(@RequestBody Map<String, String> request) {
        try {
            log.info("=== 결제 취소 요청 시작 ===");
            log.info("요청 데이터: {}", request);

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
                        log.info("결제 취소 상태 업데이트 완료: {}", merchantUid);
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
            log.error("결제 취소 실패", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "결제 취소 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/status/{merchantUid}")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String merchantUid) {
        try {
            log.info("결제 상태 조회 요청 - merchantUid: {}", merchantUid);

            Map<String, Object> payment = payments.get(merchantUid);
            if (payment == null) {
                throw new RuntimeException("주문을 찾을 수 없습니다: " + merchantUid);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("merchantUid", merchantUid);
            result.put("impUid", payment.get("impUid"));
            result.put("amount", payment.get("amount"));
            result.put("status", payment.get("status"));
            result.put("productName", payment.get("productName"));
            result.put("createdAt", payment.get("createdAt"));
            result.put("paidAt", payment.get("paidAt"));
            result.put("message", "조회 성공");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("결제 상태 조회 실패", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "결제 상태 조회 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private String getAccessToken() {
        try {
            String url = PORTONE_API_URL + "/users/getToken";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("imp_key", API_KEY);
            body.put("imp_secret", API_SECRET);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            if (jsonNode.get("code").asInt() == 0) {
                String token = jsonNode.get("response").get("access_token").asText();
                return token;
            } else {
                throw new RuntimeException("토큰 발급 실패: " + jsonNode.get("message").asText());
            }
        } catch (Exception e) {
            log.error("액세스 토큰 발급 실패", e);
            throw new RuntimeException("액세스 토큰 발급 실패", e);
        }
    }
}