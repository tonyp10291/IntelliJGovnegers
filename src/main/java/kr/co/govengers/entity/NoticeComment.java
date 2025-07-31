package kr.co.govengers.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor @Builder
@Entity
@Table(name = "notice_comment")
public class NoticeComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne
    @JoinColumn(name = "notice_id")
    private Notice notice;

    @ManyToOne
    @JoinColumn(name = "uid")
    private User user;

    private String content;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private NoticeComment parent;

    @Builder.Default
    private boolean isDeleted = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
