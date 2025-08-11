package kr.co.govengers.entity;

import jakarta.persistence.*;
import kr.co.govengers.entity.enums.AdminStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "order_info")
public class OrderInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "uid")
    private User user;

    private String utel;
    private String umail;
    private String receiverName;
    private String receiverPostcode;
    private String receiverAddress;
    private String receiverPhone;
    private String deliveryRequest;

    private Integer productTotalPrice;
    private String impUid;
    private Integer shippingCost;
    private Integer finalPayment;
    private String paymentMethod;

    @Builder.Default
    private LocalDateTime orderDate = LocalDateTime.now();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private AdminStatus adminStatus = AdminStatus.주문완료;

    @OneToMany(mappedBy = "orderInfo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;
}