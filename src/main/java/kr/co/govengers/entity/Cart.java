package kr.co.govengers.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartId;

    @ManyToOne
    @JoinColumn(name = "uid")
    private User user;

    private String guestId;

    @ManyToOne
    @JoinColumn(name = "pid")
    private Product product;

    @Builder.Default
    private Integer quantity = 1;

    private String memo;

    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();
}