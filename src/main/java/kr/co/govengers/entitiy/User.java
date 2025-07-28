package kr.com.GoGiProject.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user")
@Getter
@Setter
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

    @Column(length = 20)
    private String role;

    // 아래 3개 필드를 추가합니다.
    @Column(nullable = false)
    private boolean enabled = false;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false)
    private boolean smsVerified = false;
}