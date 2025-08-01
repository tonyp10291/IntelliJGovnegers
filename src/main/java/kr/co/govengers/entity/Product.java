package kr.co.govengers.entity;

import jakarta.persistence.*;
import kr.co.govengers.entity.enums.AdminStatus;
import kr.co.govengers.entity.enums.MainCategory;
import kr.co.govengers.entity.enums.SubCategory;
import kr.co.govengers.entity.enums.UserStatus;
import lombok.*;
import java.time.LocalDate;

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
    private SubCategory subCategory;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    private String pdesc;
    private String origin;

    private LocalDate expDate;

    @Builder.Default
    private Integer hit = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private UserStatus userStatus = UserStatus.판매중;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AdminStatus adminStatus = AdminStatus.판매중;

    private String imgFilename;
}