package kr.co.govengers.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "qna")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Qna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qid")              // ★ qid는 여기 "한 군데"만 매핑
    private Long qid;

    @Column(name = "pid", nullable = false)
    private Integer pid;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "writer_id", nullable = false, length = 50)
    private String writerId;

    @Column(name = "secret", nullable = false)
    private boolean secret;

    @Column(name = "password")
    private String password;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // ✅ OneToMany는 "mappedBy"만! (소유자는 QnaComment.qna)
    @OneToMany(mappedBy = "qna", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaComment> comments = new ArrayList<>();
}
