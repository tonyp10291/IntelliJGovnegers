package kr.co.govengers.entity;

import jakarta.persistence.*;
import kr.co.govengers.entity.enums.AdminStatus;
import kr.co.govengers.entity.enums.MainCategory;
import kr.co.govengers.entity.enums.SubCategory;
import kr.co.govengers.entity.enums.UserStatus;
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
    private Integer pid;                // 상품 고유번호

    private String pnm;                 // 상품명

    @Enumerated(EnumType.STRING)
    private MainCategory mainCategory;  // 메인카테고리 (enum)

    @Enumerated(EnumType.STRING)
    private SubCategory subCategory;

    @Builder.Default
    private Integer hit = 0;            // hit 여부(0/1)

    private Integer price;              // 가격

    private String pdesc;               // 상품 설명

    private String origin;              // 원산지

    private String image;               // 이미지 파일명/경로

    private Integer soldout;            // 품절 여부(0/1)

    private LocalDate expDate;          // 유통기한

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus = UserStatus.주문완료;   // 사용자 주문 상태

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private AdminStatus adminStatus = AdminStatus.주문완료; // 관리자 주문 상태

    // --- updateFrom 메소드 추가 (수정 편의용) ---
    public void updateFrom(Product updated) {
        this.pnm = updated.getPnm();
        this.mainCategory = updated.getMainCategory();
        this.subCategory = updated.getSubCategory();
        this.price = updated.getPrice();
        this.pdesc = updated.getPdesc();
        this.origin = updated.getOrigin();
        this.image = updated.getImage();
        this.soldout = updated.getSoldout();
        this.expDate = updated.getExpDate();
        this.userStatus = updated.getUserStatus();
        this.adminStatus = updated.getAdminStatus();
        this.hit = updated.getHit();
    }
}
