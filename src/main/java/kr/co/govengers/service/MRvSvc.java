package kr.co.govengers.service;

import kr.co.govengers.entity.Review;
import kr.co.govengers.repository.MRvRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MRvSvc {

    private final MRvRepo mRvRepo;

    @Transactional(readOnly = true)
    public Page<Review> getAllReviews(Pageable pageable) {
        return mRvRepo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Review> searchReviews(String keyword, Pageable pageable) {
        return mRvRepo.findByProductPnmContainingIgnoreCase(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Review getReviewById(Long reviewId) {
        Optional<Review> review = mRvRepo.findById(reviewId);
        return review.orElse(null);
    }

    public boolean deleteReview(Long reviewId) {
        if (mRvRepo.existsById(reviewId)) {
            mRvRepo.deleteById(reviewId);
            return true;
        }
        return false;
    }

    public Review saveResponse(Long reviewId, String responseContent) {
        Optional<Review> optionalReview = mRvRepo.findById(reviewId);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setResponse(responseContent);
            review.setResponseDate(LocalDateTime.now());
            return mRvRepo.save(review);
        }
        return null;
    }

    public Review deleteResponse(Long reviewId) {
        Optional<Review> optionalReview = mRvRepo.findById(reviewId);
        if (optionalReview.isPresent()) {
            Review review = optionalReview.get();
            review.setResponse(null);
            review.setResponseDate(null);
            return mRvRepo.save(review);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getReviewStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalReviews = mRvRepo.count();
        stats.put("totalReviews", totalReviews);
        long answeredReviews = mRvRepo.countByResponseIsNotNull();
        stats.put("answeredReviews", answeredReviews);
        long unansweredReviews = totalReviews - answeredReviews;
        stats.put("unansweredReviews", unansweredReviews);
        Double averageRating = mRvRepo.findAverageRating();
        stats.put("averageRating", averageRating != null ? Math.round(averageRating * 10) / 10.0 : 0.0);
        for (int i = 1; i <= 5; i++) {
            long count = mRvRepo.countByRating(i);
            stats.put("rating" + i + "Count", count);
        }

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long todayReviews = mRvRepo.countByCreatedAtBetween(startOfDay, endOfDay);
        stats.put("todayReviews", todayReviews);

        return stats;
    }

    @Transactional(readOnly = true)
    public Page<Review> getReviewsByUser(String userId, Pageable pageable) {
        return mRvRepo.findByUserUid(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Review> getReviewsByProduct(Long productId, Pageable pageable) {
        return mRvRepo.findByProductPid(productId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Review> getReviewsByRating(int rating, Pageable pageable) {
        return mRvRepo.findByRating(rating, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Review> getUnansweredReviews(Pageable pageable) {
        return mRvRepo.findByResponseIsNull(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Review> getAnsweredReviews(Pageable pageable) {
        return mRvRepo.findByResponseIsNotNull(pageable);
    }
}