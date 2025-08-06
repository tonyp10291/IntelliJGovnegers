package kr.co.govengers.repository;

import kr.co.govengers.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MRvRepo extends JpaRepository<Review, Long> {

    /**
     * 상품명으로 리뷰 검색 (대소문자 구분 없음)
     */
    Page<Review> findByProductPnmContainingIgnoreCase(String productName, Pageable pageable);

    /**
     * 사용자 ID로 리뷰 조회
     */
    Page<Review> findByUserUid(String uid, Pageable pageable);

    /**
     * 상품 ID로 리뷰 조회
     */
    Page<Review> findByProductPid(Long pid, Pageable pageable);

    /**
     * 별점으로 리뷰 조회
     */
    Page<Review> findByRating(int rating, Pageable pageable);

    /**
     * 별점별 리뷰 수 조회
     */
    long countByRating(int rating);

    /**
     * 답변이 없는 리뷰 조회
     */
    Page<Review> findByResponseIsNull(Pageable pageable);

    /**
     * 답변이 있는 리뷰 조회
     */
    Page<Review> findByResponseIsNotNull(Pageable pageable);

    /**
     * 답변이 있는 리뷰 수 조회
     */
    long countByResponseIsNotNull();

    /**
     * 특정 기간 내 작성된 리뷰 조회
     */
    Page<Review> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * 특정 기간 내 작성된 리뷰 수 조회
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 평균 별점 조회
     */
    @Query("SELECT AVG(r.rating) FROM Review r")
    Double findAverageRating();

    /**
     * 최신 리뷰 조회 (개수 제한)
     */
    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    Page<Review> findLatestReviews(Pageable pageable);

    /**
     * 높은 별점 리뷰 조회 (4점 이상)
     */
    @Query("SELECT r FROM Review r WHERE r.rating >= 4 ORDER BY r.rating DESC, r.createdAt DESC")
    Page<Review> findHighRatingReviews(Pageable pageable);

    /**
     * 낮은 별점 리뷰 조회 (2점 이하)
     */
    @Query("SELECT r FROM Review r WHERE r.rating <= 2 ORDER BY r.rating ASC, r.createdAt DESC")
    Page<Review> findLowRatingReviews(Pageable pageable);

    /**
     * 이미지가 있는 리뷰 조회
     */
    Page<Review> findByImgFilenameIsNotNull(Pageable pageable);

    /**
     * 특정 사용자의 리뷰 수 조회
     */
    long countByUserUid(String uid);

    /**
     * 특정 상품의 리뷰 수 조회
     */
    long countByProductPid(Long pid);

    /**
     * 특정 상품의 평균 별점 조회
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.pid = :productId")
    Double findAverageRatingByProduct(@Param("productId") Long productId);

    /**
     * 키워드로 리뷰 내용 검색
     */
    @Query("SELECT r FROM Review r WHERE r.content LIKE %:keyword% OR r.product.pnm LIKE %:keyword%")
    Page<Review> findByContentOrProductNameContaining(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 답변 미완료이면서 특정 기간 이전에 작성된 리뷰 (관리자 알림용)
     */
    @Query("SELECT r FROM Review r WHERE r.response IS NULL AND r.createdAt < :date ORDER BY r.createdAt ASC")
    Page<Review> findUnansweredReviewsOlderThan(@Param("date") LocalDateTime date, Pageable pageable);

    /**
     * 월별 리뷰 통계
     */
    @Query("SELECT MONTH(r.createdAt) as month, COUNT(r) as count FROM Review r " +
            "WHERE YEAR(r.createdAt) = :year GROUP BY MONTH(r.createdAt) ORDER BY month")
    Object[] findMonthlyReviewStats(@Param("year") int year);
}