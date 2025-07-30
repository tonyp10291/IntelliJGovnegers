package kr.co.govengers.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "user") // <- ERD/WBS에 맞게 'user' or 'users' 맞추기!
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

    @Column(length = 8)
    private String ubt;

    @Column(length = 20)
    private String utel;

    @Column(length = 100)
    private String address;   // address 필드 유지!

    @Column(length = 20)
    private String role = "USER";

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false)
    private boolean smsVerified = false;

    private int point = 0;
}
