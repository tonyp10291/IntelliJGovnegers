package kr.co.govengers.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "title", length = 50, nullable = false)
    private String title;

    @Column(name = "content", length = 1500, nullable = false)
    private String content;

    @Builder.Default
    @Column(name = "is_event", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isEvent = false;

    @Builder.Default
    @Column(name = "is_fixed", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isFixed = false;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}