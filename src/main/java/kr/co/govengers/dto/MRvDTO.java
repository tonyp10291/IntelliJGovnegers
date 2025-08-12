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
    private String response;

    public static MRvDTO from(Review review) {
        return MRvDTO.builder()
                .id(review.getReviewId())
                .productName(review.getProduct().getPnm())
                .image(review.getImgFilename())
                .rating(review.getRating())
                .content(review.getContent())
                .date(review.getCreatedAt())
                .response("")
                .build();
    }
}