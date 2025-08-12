package kr.co.govengers.repository;

import kr.co.govengers.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, String> {

    Optional<User> findByUid(String uid);
    boolean existsByUid(String uid);

    Page<User> findByUidContainingIgnoreCaseOrUnmContainingIgnoreCase(String uid, String unm, Pageable pageable);
    Page<User> findByEnabledTrue(Pageable pageable);
    Page<User> findByEnabledTrueAndUidContainingIgnoreCaseOrEnabledTrueAndUnmContainingIgnoreCase(
            String uid, String unm, Pageable pageable);
    Optional<User> findByUnmAndUtel(String unm, String utel);
    Optional<User> findByUnmAndUmail(String unm, String umail);
    Optional<User> findByUmail(String umail);
    boolean existsByUmail(String umail);
    long countByEnabledTrue();
}