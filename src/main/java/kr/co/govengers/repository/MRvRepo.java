package kr.co.govengers.repository;

import kr.co.govengers.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface MRvRepo extends JpaRepository<Review, Long> {


    long countByProduct_Pid(Integer pid);

    @Modifying
    @Transactional
    void deleteByProduct_Pid(Integer pid);


    Page<Review> findByProductPnmContainingIgnoreCase(String productName, Pageable pageable);


    Page<Review> findByUserUid(String uid, Pageable pageable);


    Page<Review> findByProductPid(Long pid, Pageable pageable);


    Page<Review> findByRating(int rating, Pageable pageable);


    long countByRating(int rating);


    Page<Review> findByResponseIsNull(Pageable pageable);


    Page<Review> findByResponseIsNotNull(Pageable pageable);


    long countByResponseIsNotNull();


    Page<Review> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);


    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);


    @Query("SELECT AVG(r.rating) FROM Review r")
    Double findAverageRating();


    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    Page<Review> findLatestReviews(Pageable pageable);


    @Query("SELECT r FROM Review r WHERE r.rating >= 4 ORDER BY r.rating DESC, r.createdAt DESC")
    Page<Review> findHighRatingReviews(Pageable pageable);


    @Query("SELECT r FROM Review r WHERE r.rating <= 2 ORDER BY r.rating ASC, r.createdAt DESC")
    Page<Review> findLowRatingReviews(Pageable pageable);


    Page<Review> findByImgFilenameIsNotNull(Pageable pageable);


    long countByUserUid(String uid);


    long countByProductPid(Long pid);


    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.pid = :productId")
    Double findAverageRatingByProduct(@Param("productId") Long productId);


    @Query("SELECT r FROM Review r WHERE r.content LIKE %:keyword% OR r.product.pnm LIKE %:keyword%")
    Page<Review> findByContentOrProductNameContaining(@Param("keyword") String keyword, Pageable pageable);


    @Query("SELECT r FROM Review r WHERE r.response IS NULL AND r.createdAt < :date ORDER BY r.createdAt ASC")
    Page<Review> findUnansweredReviewsOlderThan(@Param("date") LocalDateTime date, Pageable pageable);

    @Query("SELECT MONTH(r.createdAt) as month, COUNT(r) as count FROM Review r " +
            "WHERE YEAR(r.createdAt) = :year GROUP BY MONTH(r.createdAt) ORDER BY month")
    Object[] findMonthlyReviewStats(@Param("year") int year);
}