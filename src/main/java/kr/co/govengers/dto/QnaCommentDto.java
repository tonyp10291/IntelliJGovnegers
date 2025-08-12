// src/main/java/kr/co/govengers/dto/QnaCommentDto.java
package kr.co.govengers.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.co.govengers.entity.QnaComment;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaCommentDto {

    private Long cid;          // 댓글 PK
    private Long qid;          // 부모 QnA ID
    private String content;    // 내용
    private String writerId;   // 작성자(관리자 uid)

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public QnaCommentDto(QnaComment c) {
        if (c == null) return;
        this.cid = c.getCid();
        this.qid = (c.getQna() != null) ? c.getQna().getQid() : null;
        this.content = c.getContent();
        this.writerId = c.getWriterId();
        this.createdAt = c.getCreatedAt();
    }
}
