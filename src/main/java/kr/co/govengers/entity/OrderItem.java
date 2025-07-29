package kr.co.govengers.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderInfo orderInfo;

    @ManyToOne
    @JoinColumn(name = "pid")
    private Product product;

    private String pnm;
    private Integer price;
    private Integer quantity;
    private String imgFilename;
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.결제완료;
}