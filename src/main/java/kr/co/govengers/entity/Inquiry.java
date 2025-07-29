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
    private User user;

    private String title;
    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();
    private String answer;
    private LocalDateTime answerAt;

    @Enumerated(EnumType.STRING)
    private InquiryCategory category;

    private boolean isPrivate = false;
}