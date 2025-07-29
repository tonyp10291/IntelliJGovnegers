package kr.co.govengers.entity;

import jakarta.persistence.*;
import kr.co.govengers.entity.enums.AdminStatus;
import kr.co.govengers.entity.enums.MainCategory;
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

    private String pnm;

    @Enumerated(EnumType.STRING)
    private MainCategory mainCategory;

    private Integer hit = 0;
    private Integer price;
    private String pdesc;
    private String origin;
    private LocalDate expDate;

    @Enumerated(EnumType.STRING)
    private UserStatus userStatus = UserStatus.주문완료;

    @Enumerated(EnumType.STRING)
    private AdminStatus adminStatus = AdminStatus.주문완료;
}