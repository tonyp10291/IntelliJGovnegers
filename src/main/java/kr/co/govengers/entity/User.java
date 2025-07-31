package kr.co.govengers.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(length = 50)
    private String uid;

    @Column(nullable = false, length = 100)
    private String upw;

    @Column(nullable = false, length = 100)
    private String unm;

    @Column(unique = true, nullable = false, length = 100)
    private String umail;

    @Column(length = 20)
    private String utel;

    @Column(length = 8)
    private String ubt;

    @Column(length = 20, nullable = false)
    @Builder.Default
    private String role = "ROLE_USER";

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean smsVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private int point = 0;

    private String address;
}