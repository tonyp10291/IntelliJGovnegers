package kr.co.govengers.repository;

import kr.co.govengers.entity.Inquiry;
import kr.co.govengers.entity.enums.InquiryCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UQnARepo extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findAllByOrderByCreatedAtDesc();

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

    long countByUserUid(String userId);

    List<Inquiry> findByAnswerIsNullOrAnswerOrderByCreatedAtAsc(String s);
}