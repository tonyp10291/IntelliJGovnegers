package kr.co.govengers.entity;

import jakarta.persistence.*;
import kr.co.govengers.entity.enums.AdminStatus;
import kr.co.govengers.entity.enums.MainCategory;
import kr.co.govengers.entity.enums.SubCategory;
import kr.co.govengers.entity.enums.UserStatus;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pid;

    @Column(nullable = false)
    private String pnm;

    @Enumerated(EnumType.STRING)
    private MainCategory mainCategory;

    @Enumerated(EnumType.STRING)
    private SubCategory subCategory; // 1. subCategory 필드

    @Column(nullable = false)
    private int price;

    private String pdesc;
    private String origin; // 2. origin 필드
    private String expDate; // 3. expDate 필드

    @Column(nullable = false)
    private int stock;

    @Builder.Default
    private Integer hit = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus userStatus = UserStatus.판매중; // 4. userStatus 필드

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AdminStatus adminStatus = AdminStatus.판매중; // 5. adminStatus 필드

    private String imgFilename;
}