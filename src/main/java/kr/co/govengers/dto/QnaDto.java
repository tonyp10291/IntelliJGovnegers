package kr.co.govengers.dto;

import kr.co.govengers.entity.Qna;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class QnaDto {
    private Long qid;
    private String title;
    private String content;
    private String writerId;
    private LocalDateTime createdAt;

    public QnaDto(Qna qna) {
        this.qid = qna.getQid();
        this.title = qna.getTitle();
        this.content = qna.getContent();
        this.writerId = qna.getWriterId();
        this.createdAt = qna.getCreatedAt();
    }
}