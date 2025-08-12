package kr.co.govengers.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.co.govengers.entity.QnaComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QnaCommentDto {

    private Long cid;
    private Long qid;
    private String content;
    private String writerId;

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
