package kr.co.govengers.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_image")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pid", nullable = false)
    private Product product;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(length = 255)
    private String originalName;

    @Column(length = 20)
    private String kind;
}