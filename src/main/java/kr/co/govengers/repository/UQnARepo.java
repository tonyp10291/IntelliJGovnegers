package kr.co.govengers.repository;

import kr.co.govengers.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UQnARepo extends JpaRepository<Inquiry, Long> {
    // JpaRepository가 기본적인 DB CRUD 기능을 모두 제공합니다.
}