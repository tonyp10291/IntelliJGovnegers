package kr.com.GoGiProject.repository;

import kr.com.GoGiProject.entity.User; // User 엔티티 경로 확인
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // 이 인터페이스가 데이터베이스와 통신하는 Repository임을 나타냅니다.
public interface UserRepo extends JpaRepository<User, String> {
    // 비워두시면 됩니다.
    // JpaRepository를 상속받는 것만으로도
    // 기본적인 DB 조작(save, findById, findAll 등)이 모두 가능해집니다.
}