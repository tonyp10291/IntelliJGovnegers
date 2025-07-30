package kr.co.govengers.entity;


import jakarta.persistence.*;
import kr.co.govengers.entity.enums.AdminStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartId;

    @ManyToOne
    @JoinColumn(name = "uid")
    private User user;

    @ManyToOne
    @JoinColumn(name = "pid")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderInfo orderInfo;

    @Builder.Default
    private Integer quantity = 1;
    private Integer price;
    private String memo;
    private String receiver;
    private String tel;
    private String address;
    private String postcode;
    private String pnm;
    private String imgFilename;

    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private AdminStatus adminStatus = AdminStatus.주문완료;
}