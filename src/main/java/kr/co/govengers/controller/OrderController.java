package kr.co.govengers.controller;

import kr.co.govengers.entity.OrderInfo;
import kr.co.govengers.entity.OrderItem;
import kr.co.govengers.repository.OrderInfoRepo;
import kr.co.govengers.repository.OrderItemRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderInfoRepo orderInfoRepo;
    private final OrderItemRepo orderItemRepo;

    @GetMapping("/orders-with-items")
    public List<Map<String, Object>> getOrders(@RequestParam String uid) {
        List<OrderInfo> orders = orderInfoRepo.findByUser_UidOrderByOrderDateDesc(uid);
        List<Map<String, Object>> result = new ArrayList<>();

        for (OrderInfo o : orders) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("orderId", o.getOrderId());
            row.put("orderDate", o.getOrderDate());
            row.put("adminStatus", o.getAdminStatus());
            row.put("impUid", o.getImpUid());
            row.put("finalPayment", o.getFinalPayment());
            row.put("shippingCost", o.getShippingCost());
            row.put("productTotalPrice", o.getProductTotalPrice());
            row.put("paymentMethod", o.getPaymentMethod());

            row.put("receiverName", o.getReceiverName());
            row.put("receiverPhone", o.getReceiverPhone());
            row.put("receiverAddress", o.getReceiverAddress());
            row.put("receiverPostcode", o.getReceiverPostcode());
            row.put("deliveryRequest", o.getDeliveryRequest());

            List<OrderItem> items = orderItemRepo.findByOrderInfo_OrderId(o.getOrderId());
            List<Map<String, Object>> mapped = new ArrayList<>();
            for (OrderItem it : items) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", it.getId());
                m.put("pid", it.getProduct() != null ? it.getProduct().getPid() : null);
                m.put("pnm", it.getPnm());
                m.put("price", it.getPrice());
                m.put("quantity", it.getQuantity());
                m.put("imgFilename", it.getImgFilename());
                m.put("status", it.getStatus());
                mapped.add(m);
            }
            row.put("items", mapped);
            result.add(row);
        }
        return result;
    }
}
