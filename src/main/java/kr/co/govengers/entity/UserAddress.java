package kr.co.govengers.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "user_address")
public class UserAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @ManyToOne
    @JoinColumn(name = "uid")
    private User user;

    private String receiverName;
    private String phone;
    private String postcode;
    private String address;
    private boolean isDefault = false;
}