package kr.co.govengers.repository;

import kr.co.govengers.entity.EmailToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailTokenRepo extends JpaRepository<EmailToken, Long> {
    Optional<EmailToken> findByEmailAndToken(String email, String token);
}