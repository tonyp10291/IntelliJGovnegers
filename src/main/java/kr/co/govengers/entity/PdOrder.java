package kr.co.govengers.entity;//package kr.co.govengers.entity;
//
//import jakarta.persistence.*;
//import kr.co.govengers.entity.enums.OrderStatus;
//import kr.co.govengers.entity.enums.PaymentMethod;
//import lombok.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
//@Entity
//@Table(name = "pd_order")
//public class PdOrder {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long orderId;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "uid")
//    private User user;
//
//    @Column(nullable = false, unique = true)
//    private String orderNumber; // 주문번호 (예: ORD-20250801-001)
//
//    @Column(nullable = false)
//    private Integer totalAmount; // 총 주문금액
//
//    @Column(nullable = false)
//    private Integer totalQuantity; // 총 주문수량
//
//    @Builder.Default
//    @Enumerated(EnumType.STRING)
//    private OrderStatus orderStatus = OrderStatus.PENDING; // 주문상태
//
//    @Enumerated(EnumType.STRING)
//    private PaymentMethod paymentMethod; // 결제수단
//
//    @Builder.Default
//    private LocalDateTime orderDate = LocalDateTime.now(); // 주문일시
//
//    private LocalDateTime cancelDate; // 취소일시
//
//    private String cancelReason; // 취소사유
//
//    // 배송 정보
//    private String deliveryAddress; // 배송주소
//    private String deliveryPhone; // 배송연락처
//    private String deliveryMemo; // 배송메모
//
//    // 주문자 정보 (스냅샷)
//    private String orderUserName; // 주문 당시 사용자명
//    private String orderUserPhone; // 주문 당시 연락처
//    private String orderUserEmail; // 주문 당시 이메일
//
//    // 주문 상품 목록
//    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<PdOrderItem> orderItems;
//
//    // 편의 메서드
//    public boolean isCancellable() {
//        return orderStatus == OrderStatus.PENDING || orderStatus == OrderStatus.CONFIRMED;
//    }
//
//    public boolean isStatusChangeable() {
//        return orderStatus != OrderStatus.CANCELLED && orderStatus != OrderStatus.DELIVERED;
//    }
//}