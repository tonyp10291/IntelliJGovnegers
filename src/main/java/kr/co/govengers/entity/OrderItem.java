package kr.co.govengers.entity;

import jakarta.persistence.*;
import kr.co.govengers.entity.enums.PaymentStatus;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderInfo orderInfo;

    @ManyToOne
    @JoinColumn(name = "pid")
    private Product product;

    private String pnm;
    private Integer price;
    private Integer quantity;
    private String imgFilename;
    private String paymentMethod;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.결제완료;
}
