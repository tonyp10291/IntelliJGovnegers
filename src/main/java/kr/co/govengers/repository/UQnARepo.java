package kr.co.govengers.repository;

import kr.co.govengers.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UQnARepo extends JpaRepository<Inquiry, Long> {
}