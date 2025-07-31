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

    // 전체 목록 페이징 (최신순)
    Page<Inquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 카테고리별 조회
    Page<Inquiry> findByCategoryOrderByCreatedAtDesc(InquiryCategory category, Pageable pageable);

    // 답변 상태별 조회 (답변 유무로 판단)
    @Query("SELECT i FROM Inquiry i WHERE " +
            "(:answerStatus = 'PENDING' AND i.answer IS NULL) OR " +
            "(:answerStatus = 'ANSWERED' AND i.answer IS NOT NULL) " +
            "ORDER BY i.createdAt DESC")
    Page<Inquiry> findByAnswerStatusOrderByCreatedAtDesc(@Param("answerStatus") String answerStatus, Pageable pageable);

    // 공개/비공개별 조회
    Page<Inquiry> findByIsPrivateOrderByCreatedAtDesc(boolean isPrivate, Pageable pageable);

    // 검색 기능 (제목, 내용, 작성자명으로 검색)
    @Query("SELECT i FROM Inquiry i WHERE " +
            "i.title LIKE %:keyword% OR " +
            "i.content LIKE %:keyword% OR " +
            "i.user.unm LIKE %:keyword% " +
            "ORDER BY i.createdAt DESC")
    Page<Inquiry> findByKeywordOrderByCreatedAtDesc(@Param("keyword") String keyword, Pageable pageable);

    // 통계용 쿼리들
    long count(); // 전체 문의 개수

    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.answer IS NULL")
    long countPending(); // 답변 대기 중인 문의 개수

    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.answer IS NOT NULL")
    long countAnswered(); // 답변 완료된 문의 개수

    long countByIsPrivateTrue(); // 비공개 문의 개수
}