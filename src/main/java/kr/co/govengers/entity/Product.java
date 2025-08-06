package kr.co.govengers.entity;

import jakarta.persistence.*;
import kr.co.govengers.entity.enums.AdminStatus;
import kr.co.govengers.entity.enums.MainCategory;
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
    private Integer pid;

    @Column(nullable = false)
    private String pnm;

    @Enumerated(EnumType.STRING)
    private MainCategory mainCategory;

    @Column(nullable = false)
    private Integer price;

    private String pdesc;
    private String origin;
    private LocalDate expDate;

    @Builder.Default
    private Integer hit = 0;

    private String image;

    @Builder.Default
    private Integer soldout = 0;

    public void updateFrom(Product updated) {
        if (updated.getPnm() != null) this.pnm = updated.getPnm();
        if (updated.getMainCategory() != null) this.mainCategory = updated.getMainCategory();
        if (updated.getPrice() != null) this.price = updated.getPrice();
        if (updated.getPdesc() != null) this.pdesc = updated.getPdesc();
        if (updated.getOrigin() != null) this.origin = updated.getOrigin();
        if (updated.getExpDate() != null) this.expDate = updated.getExpDate();
        if (updated.getHit() != null) this.hit = updated.getHit();
        if (updated.getImage() != null) this.image = updated.getImage();
        if (updated.getSoldout() != null) this.soldout = updated.getSoldout();
    }
    public int calculatePoint(){
        return (int) (this.price * 0.05);
    }
    public int shippingCost(){
        return 3500;
    }
}