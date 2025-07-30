package kr.co.govengers.repository;

import kr.co.govengers.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<Users, String> {
        Optional<Users> findByUnmAndUtel(String unm, String utel); // 이 메소드 추가
    }
