package kr.co.govengers.entity;


import jakarta.persistence.*;
import kr.co.govengers.entity.enums.InquiryCategory;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "inquiry")
public class Inquiry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inquiryId;

    @ManyToOne
    @JoinColumn(name = "uid")
    private Users user;

    private String title;
    private String content;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private String answer;
    private LocalDateTime answerAt;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private InquiryCategory category = InquiryCategory.상품문의;

    @Builder.Default
    private boolean isPrivate = false;
}