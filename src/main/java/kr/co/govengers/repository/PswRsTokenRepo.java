package kr.co.govengers.repository;

import kr.co.govengers.entity.PswRsToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PswRsTokenRepo extends JpaRepository<PswRsToken, Long> {
    Optional<PswRsToken> findByToken(String token);
}