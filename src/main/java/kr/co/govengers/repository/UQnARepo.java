package kr.co.govengers.repository;

import kr.co.govengers.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UQnARepo extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findAllByOrderByCreatedAtDesc();

    @Query("SELECT i FROM Inquiry i WHERE " +
            "(i.isPrivate = false) OR " +
            "(i.isPrivate = true AND i.user.uid = :userId) " +
            "ORDER BY i.createdAt DESC")
    List<Inquiry> findAllWithVisibilityForUser(@Param("userId") String userId);

    @Query("SELECT i FROM Inquiry i WHERE " +
            "i.category = :category AND " +
            "((i.isPrivate = false) OR (i.isPrivate = true AND i.user.uid = :userId)) " +
            "ORDER BY i.createdAt DESC")
    List<Inquiry> findByCategoryAndVisibilityForUser(
            @Param("category") String category,
            @Param("userId") String userId
    );

    @Query("SELECT i FROM Inquiry i WHERE " +
            "i.title LIKE %:keyword% AND " +
            "((i.isPrivate = false) OR (i.isPrivate = true AND i.user.uid = :userId)) " +
            "ORDER BY i.createdAt DESC")
    List<Inquiry> findByTitleContainingAndVisibilityForUser(
            @Param("keyword") String keyword,
            @Param("userId") String userId
    );

    @Query("SELECT i FROM Inquiry i WHERE " +
            "i.category = :category AND " +
            "i.title LIKE %:keyword% AND " +
            "((i.isPrivate = false) OR (i.isPrivate = true AND i.user.uid = :userId)) " +
            "ORDER BY i.createdAt DESC")
    List<Inquiry> findByCategoryAndTitleContainingAndVisibilityForUser(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("userId") String userId
    );
}