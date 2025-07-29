package kr.co.govengers.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pid;

    private String pnm;

    @Enumerated(EnumType.STRING)
    private MainCategory mainCategory;

    @Enumerated(EnumType.STRING)
    private SubCategory subCategory;

    private Integer hit = 0;
    private Integer price;
    private String pdesc;
    private String origin;
    private LocalDate expDate;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus = UserStatus.주문완료;

    @Enumerated(EnumType.STRING)
    private AdminStatus adminStatus = AdminStatus.주문완료;

    // 이미지 경로 컬럼 필요하다면 아래 추가 (DB에도 컬럼 추가!)
    private String imageUrl;
}
