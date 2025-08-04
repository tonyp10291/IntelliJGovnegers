package kr.co.govengers.repository;

import kr.co.govengers.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NTRepo extends JpaRepository<Notice, Long> {
    List<Notice> findAllByOrderByIsFixedDescCreatedAtDesc();
}