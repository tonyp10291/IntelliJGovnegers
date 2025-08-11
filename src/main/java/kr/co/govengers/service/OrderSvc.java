package kr.co.govengers.service;

import kr.co.govengers.entity.*;
import kr.co.govengers.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderSvc {

    private final OrderInfoRepo orderInfoRepo;
    private final OrderItemRepo orderItemRepo;
    private final UserRepo userRepo;
    private final PdRepo pdRepo;

    public Long saveFromPaymentMap(Map<String, Object> payment, String impUid) {
        log.info("=== 주문 저장 시작 ===");
        log.info("payment 데이터: {}", payment);

        // 사용자
        String uid = (String) payment.get("userId");
        User user = (uid != null && !uid.isBlank()) ? userRepo.findById(uid).orElse(null) : null;

        // 금액/배송
        Integer finalPayment = toInt(payment.get("amount"));
        Integer shippingCost = payment.get("shippingCost") != null ? toInt(payment.get("shippingCost")) : 3500;
        Integer productTotalPrice = (finalPayment != null && shippingCost != null)
                ? finalPayment - shippingCost
                : toInt(payment.get("productPrice"));

        log.info("finalPayment: {}, shippingCost: {}, productTotalPrice: {}",
                finalPayment, shippingCost, productTotalPrice);

        // 주문정보 저장
        OrderInfo orderInfo = OrderInfo.builder()
                .user(user)
                .utel((String) payment.get("buyerPhone"))
                .umail((String) payment.get("buyerEmail"))
                .receiverName((String) payment.get("receiverName"))
                .receiverPostcode((String) payment.get("zipCode"))
                .receiverAddress(((string(payment.get("address")) + " " + string(payment.get("detailAddress"))).trim()))
                .receiverPhone((String) payment.get("buyerPhone"))
                .deliveryRequest((String) payment.get("deliveryMemo"))
                .productTotalPrice(productTotalPrice)
                .impUid(impUid)
                .shippingCost(shippingCost)
                .finalPayment(finalPayment)
                .paymentMethod((String) payment.get("payMethod"))
                .build();
        orderInfoRepo.save(orderInfo);

        log.info("OrderInfo 저장 완료 - orderId: {}", orderInfo.getOrderId());

        // ✅ 여러 상품 처리
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> productInfoList = (List<Map<String, Object>>) payment.get("productInfoList");

        if (productInfoList != null && !productInfoList.isEmpty()) {
            log.info("productInfoList 개수: {}", productInfoList.size());

            // 여러 상품을 각각 OrderItem으로 저장
            for (int i = 0; i < productInfoList.size(); i++) {
                Map<String, Object> productInfo = productInfoList.get(i);
                log.info("상품 {} 처리 시작: {}", i + 1, productInfo);

                Integer pid = toInt(productInfo.get("productId"));
                Product product = (pid != null) ? pdRepo.findById(pid).orElse(null) : null;

                OrderItem item = OrderItem.builder()
                        .orderInfo(orderInfo)
                        .product(product)
                        .pnm((String) productInfo.getOrDefault("productName",
                                product != null ? product.getPnm() : "상품명 없음"))
                        .price(toInt(productInfo.get("productPrice")))
                        .quantity(toInt(productInfo.get("quantity")))
                        .imgFilename(product != null ? product.getImage() : null)
                        .paymentMethod((String) payment.get("payMethod"))
                        .build();

                orderItemRepo.save(item);

                log.info("OrderItem {} 저장 완료 - pid: {}, pnm: {}, quantity: {}, price: {}",
                        i + 1, pid, item.getPnm(), item.getQuantity(), item.getPrice());
            }

        } else {
            // ✅ productInfoList가 없으면 기존 방식으로 fallback
            log.warn("productInfoList가 없어서 기존 방식으로 저장합니다.");

            Integer pid = toInt(payment.get("productId"));
            Product product = (pid != null) ? pdRepo.findById(pid).orElse(null) : null;

            OrderItem item = OrderItem.builder()
                    .orderInfo(orderInfo)
                    .product(product)  // ✅ Product 엔티티로 관계 설정
                    .pnm((String) payment.getOrDefault("productName",
                            product != null ? product.getPnm() : "상품명 없음"))
                    .price(toInt(payment.get("productPrice")))
                    .quantity(toInt(payment.get("quantity")))
                    .imgFilename(product != null ? product.getImage() : null)
                    .paymentMethod((String) payment.get("payMethod"))
                    .build();

            orderItemRepo.save(item);

            log.info("Fallback OrderItem 저장 완료 - productId: {}, pnm: {}, quantity: {}, price: {}",
                    pid, item.getPnm(), item.getQuantity(), item.getPrice());
        }

        log.info("=== 주문 저장 완료 - orderId: {} ===", orderInfo.getOrderId());
        return orderInfo.getOrderId();
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Integer i) return i;
        if (o instanceof Long l) return l.intValue();
        if (o instanceof Double d) return d.intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String string(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}