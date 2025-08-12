package kr.co.govengers.service;

import kr.co.govengers.entity.*;
import kr.co.govengers.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderSvc {

    private final OrderInfoRepo orderInfoRepo;
    private final OrderItemRepo orderItemRepo;
    private final UserRepo userRepo;
    private final PdRepo pdRepo;

    public Long saveFromPaymentMap(Map<String, Object> payment, String impUid) {
        // 사용자
        String uid = (String) payment.get("userId");
        User user = (uid != null && !uid.isBlank()) ? userRepo.findById(uid).orElse(null) : null;

        // 금액/배송
        Integer finalPayment = toInt(payment.get("amount"));
        Integer shippingCost = payment.get("shippingCost") != null ? toInt(payment.get("shippingCost")) : 3500;
        Integer productTotalPrice = (finalPayment != null && shippingCost != null)
                ? finalPayment - shippingCost
                : toInt(payment.get("productPrice"));

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

        Integer pid = toInt(payment.get("productId"));
        Product product = (pid != null) ? pdRepo.findById(pid).orElse(null) : null;

        OrderItem item = OrderItem.builder()
                .orderInfo(orderInfo)
                .product(product)
                .pnm((String) payment.getOrDefault("productName", product != null ? product.getPnm() : null))
                .price(finalPayment)
                .quantity(toInt(payment.get("quantity")))
                .imgFilename(product != null ? product.getImage() : null)
                .paymentMethod((String) payment.get("payMethod"))
                .build();
        orderItemRepo.save(item);

        return orderInfo.getOrderId();
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Integer i) return i;
        if (o instanceof Long l) return l.intValue();
        if (o instanceof Double d) return d.intValue();
        return Integer.parseInt(String.valueOf(o));
    }

    private String string(Object o) {
        return o == null ? "" : String.valueOf(o);
    }
}
