package kr.co.govengers.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    private String uid;

    private String upw;
    private String unm;
    private String umail;
    private String ubt;
    private String utel;

    private String role = "USER";
    private boolean enabled = false;
    private boolean emailVerified = false;
    private boolean smsVerified = false;

    private int point = 0;
}