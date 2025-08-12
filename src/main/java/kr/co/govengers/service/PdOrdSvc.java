package kr.co.govengers.service;

import kr.co.govengers.entity.*;
import kr.co.govengers.entity.enums.AdminStatus;
import kr.co.govengers.entity.enums.PaymentStatus;
import kr.co.govengers.repository.OrderItemRepo;
import kr.co.govengers.repository.PdOrdRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PdOrdSvc {

    private final PdOrdRepo pdOrdRepo;
    private final OrderItemRepo orderItemRepo;

    public Page<Map<String, Object>> getOrdersWithPaging(
            Pageable pageable,
            String keyword,
            AdminStatus orderStatus,
            String paymentMethod,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        Page<OrderInfo> page = pdOrdRepo.findOrdersWithFilters(
                pageable, keyword, orderStatus, paymentMethod, startDate, endDate);

        List<Map<String, Object>> mapped = page.getContent().stream()
                .map(this::convertOrderToFrontendFormat)
                .collect(Collectors.toList());

        return new PageImpl<>(mapped, pageable, page.getTotalElements());
    }

    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> st = new HashMap<>();
        long pending = pdOrdRepo.countByAdminStatus(AdminStatus.주문완료);
        long confirmed = pdOrdRepo.countByAdminStatus(AdminStatus.주문완료);
        long shipping = pdOrdRepo.countByAdminStatus(AdminStatus.배송중);

        LocalDateTime startOfDay = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        Long todayOrders = pdOrdRepo.countByOrderDateBetween(startOfDay, endOfDay);
        Integer todayAmount = pdOrdRepo.sumFinalPaymentByOrderDateBetween(startOfDay, endOfDay);

        st.put("pendingOrders", pending);
        st.put("confirmedOrders", confirmed);
        st.put("shippingOrders", shipping);
        st.put("todayOrders", (todayOrders != null) ? todayOrders : 0);
        st.put("todayAmount", (todayAmount != null) ? todayAmount : 0);
        return st;
    }

    public void updateOrderStatus(Long orderId, AdminStatus status) {
        OrderInfo order = pdOrdRepo.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        order.setAdminStatus(status);

        if (status == AdminStatus.주문취소완료) {
            List<OrderItem> items = pdOrdRepo.findOrderItemsByOrderId(order.getOrderId());
            items.forEach(i -> i.setStatus(PaymentStatus.환불완료));
            orderItemRepo.saveAll(items);
        }
        pdOrdRepo.save(order);
    }

    public void cancelWholeOrderAndRefund(Long orderId, String reason) {
        OrderInfo order = pdOrdRepo.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        if (order.getAdminStatus() == AdminStatus.주문취소완료) {
            System.out.println("이미 취소된 주문입니다: " + orderId);
            return;
        }

        if (order.getAdminStatus() == AdminStatus.배송완료) {
            throw new RuntimeException("배송완료된 주문은 취소할 수 없습니다.");
        }

        List<OrderItem> items = pdOrdRepo.findOrderItemsByOrderId(order.getOrderId());
        items.forEach(i -> i.setStatus(PaymentStatus.환불완료));
        orderItemRepo.saveAll(items);

        order.setAdminStatus(AdminStatus.주문취소완료);
        pdOrdRepo.save(order);

        System.out.println("주문 전체 취소 완료 - orderId: " + orderId + ", impUid: " + order.getImpUid());
    }

    public void cancelOrderItemWithPayment(Long itemId, String reason) {
        OrderItem item = pdOrdRepo.findOrderItemById(itemId)
                .orElseThrow(() -> new RuntimeException("주문 상품을 찾을 수 없습니다."));

        if (item.getStatus() == PaymentStatus.환불완료) {
            throw new RuntimeException("이미 취소된 상품입니다.");
        }

        OrderInfo order = item.getOrderInfo();
        int amount = (item.getPrice() == null ? 0 : item.getPrice())
                * (item.getQuantity() == null ? 0 : item.getQuantity());

        item.setStatus(PaymentStatus.환불완료);
        orderItemRepo.save(item);

        boolean allRefunded = pdOrdRepo.findOrderItemsByOrderId(order.getOrderId()).stream()
                .allMatch(oi -> oi.getStatus() == PaymentStatus.환불완료);
        if (allRefunded) {
            order.setAdminStatus(AdminStatus.주문취소완료);
            pdOrdRepo.save(order);
        }

        System.out.println("개별 상품 취소 완료 - itemId: " + itemId +
                ", amount: " + amount + ", impUid: " + order.getImpUid());
    }

    public Map<String, Object> getOrderDetailForAdmin(Long orderId) {
        OrderInfo orderInfo = pdOrdRepo.findByIdWithUserAndItems(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        return convertOrderToFrontendFormat(orderInfo);
    }

    public void bulkUpdateOrderStatus(List<Long> orderIds, AdminStatus status) {
        List<OrderInfo> orders = pdOrdRepo.findAllById(orderIds);
        for (OrderInfo order : orders) {
            order.setAdminStatus(status);
            if (status == AdminStatus.주문취소완료) {
                List<OrderItem> items = pdOrdRepo.findOrderItemsByOrderId(order.getOrderId());
                items.forEach(i -> i.setStatus(PaymentStatus.환불완료));
                orderItemRepo.saveAll(items);
            }
        }
        pdOrdRepo.saveAll(orders);
    }

    public Map<String, Object> getOrderInfoForCancel(Long orderId) {
        OrderInfo order = pdOrdRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getOrderId());
        result.put("impUid", order.getImpUid());
        result.put("finalPayment", order.getFinalPayment());
        result.put("adminStatus", order.getAdminStatus().name());

        return result;
    }

    public Map<String, Object> getOrderItemInfoForCancel(Long itemId) {
        OrderItem item = pdOrdRepo.findOrderItemById(itemId)
                .orElseThrow(() -> new RuntimeException("주문 상품을 찾을 수 없습니다."));

        OrderInfo order = item.getOrderInfo();
        int amount = (item.getPrice() == null ? 0 : item.getPrice())
                * (item.getQuantity() == null ? 0 : item.getQuantity());

        Map<String, Object> result = new HashMap<>();
        result.put("orderItemId", item.getId());
        result.put("orderId", order.getOrderId());
        result.put("impUid", order.getImpUid());
        result.put("productName", item.getPnm());
        result.put("cancelAmount", amount);
        result.put("status", item.getStatus().name());

        return result;
    }

    private Map<String, Object> convertOrderToFrontendFormat(OrderInfo order) {
        Map<String, Object> m = new LinkedHashMap<>();

        m.put("orderId", order.getOrderId());
        m.put("impUid", order.getImpUid());
        m.put("orderDate", order.getOrderDate() != null
                ? order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
        m.put("adminStatus", order.getAdminStatus() != null ? order.getAdminStatus().name() : null);
        m.put("finalPayment", order.getFinalPayment());
        m.put("paymentMethod", order.getPaymentMethod());

        User user = order.getUser();
        m.put("uid", user != null ? user.getUid() : null);
        m.put("receiverName", order.getReceiverName());
        m.put("receiverPostcode", order.getReceiverPostcode());
        m.put("receiverAddress", order.getReceiverAddress());
        m.put("receiverPhone", order.getReceiverPhone());
        m.put("deliveryRequest", order.getDeliveryRequest());

        List<OrderItem> orderItems = pdOrdRepo.findOrderItemsByOrderId(order.getOrderId());

        List<Map<String, Object>> items = orderItems.stream().map(oi -> {
            Map<String, Object> im = new HashMap<>();
            Integer price = oi.getPrice();
            Integer qty = oi.getQuantity();

            int unitPrice = (price == null ? 0 : price);
            int quantity = (qty == null ? 0 : qty);
            int subtotal = unitPrice * quantity;

            im.put("id", oi.getId());
            im.put("orderItemId", oi.getId());
            im.put("pnm", oi.getPnm());
            im.put("name", oi.getPnm());
            im.put("quantity", quantity);
            im.put("price", unitPrice);

            im.put("unitPrice", unitPrice);
            im.put("subtotal", subtotal);
            im.put("totalPrice", subtotal);

            im.put("status", oi.getStatus() != null ? oi.getStatus().name() : null);
            im.put("imgFilename", oi.getImgFilename());

            if (oi.getImgFilename() != null) {
                String enc = URLEncoder.encode(oi.getImgFilename(), StandardCharsets.UTF_8);
                im.put("imageUrl", "/api/images/" + enc);
            } else {
                im.put("imageUrl", null);
            }
            return im;
        }).collect(Collectors.toList());
        m.put("items", items);

        int productTotal = orderItems.stream()
                .mapToInt(oi -> (oi.getPrice() == null ? 0 : oi.getPrice())
                        * (oi.getQuantity() == null ? 0 : oi.getQuantity()))
                .sum();
        m.put("productTotalPrice", productTotal);
        m.put("shippingCost", order.getShippingCost() == null ? 0 : order.getShippingCost());

        m.put("orderNumber", String.format("ORD%06d", order.getOrderId()));
        m.put("orderStatus", convertToFrontendStatus(order.getAdminStatus()));
        m.put("totalAmount", order.getFinalPayment());
        m.put("deliveryAddress", String.format("[%s] %s",
                order.getReceiverPostcode() != null ? order.getReceiverPostcode() : "",
                order.getReceiverAddress() != null ? order.getReceiverAddress() : ""));
        m.put("deliveryPhone", order.getReceiverPhone());
        m.put("deliveryMemo", order.getDeliveryRequest());

        List<Map<String, Object>> orderItemsList = orderItems.stream()
                .map(this::convertOrderItemToFrontendFormat)
                .collect(Collectors.toList());
        m.put("orderItems", orderItemsList);

        return m;
    }

    private Map<String, Object> convertOrderItemToFrontendFormat(OrderItem item) {
        Map<String, Object> m = new HashMap<>();
        Integer price = item.getPrice();
        Integer qty   = item.getQuantity();

        m.put("orderItemId", item.getId());
        m.put("productName", item.getPnm());
        m.put("productOptions", "");
        m.put("quantity", qty == null ? 0 : qty);
        m.put("unitPrice", price == null ? 0 : price);
        m.put("totalPrice", (price == null ? 0 : price) * (qty == null ? 0 : qty));
        m.put("status", item.getStatus() != null ? item.getStatus().name() : null);
        return m;
    }

    private String convertToFrontendStatus(AdminStatus s) {
        Map<AdminStatus, String> map = new HashMap<>();
        map.put(AdminStatus.주문완료, "CONFIRMED");
        map.put(AdminStatus.주문취소요청, "CANCEL_REQUESTED");
        map.put(AdminStatus.주문취소완료, "CANCELLED");
        map.put(AdminStatus.배송준비중, "PREPARING");
        map.put(AdminStatus.배송중, "SHIPPING");
        map.put(AdminStatus.배송완료, "DELIVERED");
        map.put(AdminStatus.주문실패, "FAILED");
        return map.getOrDefault(s, "CONFIRMED");
    }
}