package kr.co.govengers.repository;

import kr.co.govengers.entity.Inquiry;
import kr.co.govengers.entity.enums.InquiryCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UQnARepo extends JpaRepository<Inquiry, Long> {

    // 내 문의내역 (uid 기준, 정렬)
    List<Inquiry> findByUserUidOrderByCreatedAtDesc(String uid);

    // 내 문의내역 + 카테고리
    List<Inquiry> findByUserUidAndCategory(String uid, InquiryCategory category);

    // 내 문의내역 + 제목 키워드
    List<Inquiry> findByUserUidAndTitleContaining(String uid, String keyword);

    // 내 문의내역 + 카테고리 + 제목 키워드
    List<Inquiry> findByUserUidAndCategoryAndTitleContaining(String uid, InquiryCategory category, String keyword);

    // ==============================
    // 전체 문의 목록 (공개/비공개 필터: 기존 Q&A 페이지 용)
    List<Inquiry> findAllByOrderByCreatedAtDesc();

    // 전체 문의 목록(공개/비공개 정책)
    @Query("SELECT i FROM Inquiry i ORDER BY i.createdAt DESC")
    List<Inquiry> findAllWithVisibilityForUser(@Param("userId") String userId);

    @Query("SELECT i FROM Inquiry i WHERE i.category = :category ORDER BY i.createdAt DESC")
    List<Inquiry> findByCategoryAndVisibilityForUser(
            @Param("category") InquiryCategory category,
            @Param("userId") String userId
    );

    @Query("SELECT i FROM Inquiry i WHERE (i.title LIKE %:keyword% OR i.user.uid LIKE %:keyword%) ORDER BY i.createdAt DESC")
    List<Inquiry> findByTitleOrUserUidContainingAndVisibilityForUser(
            @Param("keyword") String keyword,
            @Param("userId") String userId
    );

    @Query("SELECT i FROM Inquiry i WHERE i.category = :category AND (i.title LIKE %:keyword% OR i.user.uid LIKE %:keyword%) ORDER BY i.createdAt DESC")
    List<Inquiry> findByCategoryAndTitleOrUserUidContainingAndVisibilityForUser(
            @Param("category") InquiryCategory category,
            @Param("keyword") String keyword,
            @Param("userId") String userId
    );

    // 기타 서브기능
    long countByUserUid(String userId);

    List<Inquiry> findByAnswerIsNullOrAnswerOrderByCreatedAtAsc(String s);
}