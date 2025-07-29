package kr.co.govengers.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @ManyToOne
    @JoinColumn(name = "pid")
    private Product product;

    private String filename;
    private String url;

    @Enumerated(EnumType.STRING)
    private ImageType type = ImageType.대표;
}