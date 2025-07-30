//추가
package kr.co.govengers.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_token")
@Getter
@Setter
@NoArgsConstructor
public class EmailToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private boolean isVerified = false;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Builder
    public EmailToken(String email, String token, boolean isVerified, LocalDateTime expiryDate) {
        this.email = email;
        this.token = token;
        this.isVerified = isVerified;
        this.expiryDate = expiryDate;
    }
}