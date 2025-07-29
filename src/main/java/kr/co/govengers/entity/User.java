package kr.co.govengers.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
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

    @Column(length = 100)
    private String address;

    private int point = 0;

    @Column(length = 20)
    private String role;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false)
    private boolean smsVerified = false;
}
