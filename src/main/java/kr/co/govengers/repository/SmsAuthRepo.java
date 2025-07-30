package kr.co.govengers.repository;

import kr.co.govengers.entity.SmsAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsAuthRepo extends JpaRepository<SmsAuth, String> {
}