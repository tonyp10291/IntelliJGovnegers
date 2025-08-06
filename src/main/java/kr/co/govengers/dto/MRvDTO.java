package kr.co.govengers.dto;

import kr.co.govengers.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MRvDTO {
    private Long id;
    private String productName;
    private String image;
    private int rating;
    private String content;
    private LocalDateTime date;
    private String response; // 관리자 답변 (필요시)

    /**
     * Review 엔티티를 DTO로 변환
     */
    public static MRvDTO from(Review review) {
        return MRvDTO.builder()
                .id(review.getReviewId())
                .productName(review.getProduct().getPnm()) // Product 엔티티의 pnm 필드 사용
                .image(review.getImgFilename())
                .rating(review.getRating())
                .content(review.getContent())
                .date(review.getCreatedAt())
                .response("") // 현재는 빈 값, 나중에 답변 기능 추가시 수정
                .build();
    }
}