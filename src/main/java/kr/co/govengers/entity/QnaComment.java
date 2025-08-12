// src/main/java/kr/co/govengers/entity/QnaComment.java
package kr.co.govengers.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "qna_comment")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class QnaComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cid")
    private Long cid;

    // ✅ 다대일 소유자: qna_comment.qid -> qna.qid
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qid", nullable = false)
    private Qna qna;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "writer_id", nullable = false, length = 50)
    private String writerId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
