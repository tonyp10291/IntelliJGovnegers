package kr.co.govengers.repository;

import kr.co.govengers.entity.Inquiry;
import kr.co.govengers.entity.enums.InquiryCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MQnARepo extends JpaRepository<Inquiry, Long> {

    Page<Inquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Inquiry> findByCategoryOrderByCreatedAtDesc(InquiryCategory category, Pageable pageable);

    @Query("SELECT i FROM Inquiry i WHERE " +
            "(:answerStatus = 'PENDING' AND i.answer IS NULL) OR " +
            "(:answerStatus = 'ANSWERED' AND i.answer IS NOT NULL) " +
            "ORDER BY i.createdAt DESC")
    Page<Inquiry> findByAnswerStatusOrderByCreatedAtDesc(@Param("answerStatus") String answerStatus, Pageable pageable);

    Page<Inquiry> findByIsPrivateOrderByCreatedAtDesc(boolean isPrivate, Pageable pageable);

    @Query("SELECT i FROM Inquiry i WHERE " +
            "i.title LIKE %:keyword% OR " +
            "i.content LIKE %:keyword% OR " +
            "i.user.unm LIKE %:keyword% " +
            "ORDER BY i.createdAt DESC")
    Page<Inquiry> findByKeywordOrderByCreatedAtDesc(@Param("keyword") String keyword, Pageable pageable);

    long count();

    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.answer IS NULL")
    long countPending();

    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.answer IS NOT NULL")
    long countAnswered();

    long countByIsPrivateTrue();
}