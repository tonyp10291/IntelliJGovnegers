//추가
package kr.co.govengers.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sms_auth")
@Getter
@Setter
public class SmsAuth {

    @Id
    private String phone;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private long createdTime;
}