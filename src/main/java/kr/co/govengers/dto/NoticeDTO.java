package kr.co.govengers.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.co.govengers.entity.Notice;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NoticeDTO {
    private Long noticeId;
    private String title;
    private String content;

    @JsonProperty("isEvent")
    private boolean isEvent;

    @JsonProperty("isFixed")
    private boolean isFixed;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    public NoticeDTO() {}

    public NoticeDTO(Notice notice) {
        this.noticeId = notice.getNoticeId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.isEvent = notice.isEvent();
        this.isFixed = notice.isFixed();
        this.createdAt = notice.getCreatedAt();

        System.out.println("🔄 DTO 변환: " + notice.getNoticeId() +
                " | isEvent=" + this.isEvent +
                " | isFixed=" + this.isFixed);
    }

    public Notice toEntity() {
        return Notice.builder()
                .title(this.title)
                .content(this.content)
                .isEvent(this.isEvent)
                .isFixed(this.isFixed)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public boolean isEvent() {
        return this.isEvent;
    }

    public boolean isFixed() {
        return this.isFixed;
    }

    @JsonProperty("isEvent")
    public boolean getIsEvent() {
        return this.isEvent;
    }

    @JsonProperty("isFixed")
    public boolean getIsFixed() {
        return this.isFixed;
    }

    @Override
    public String toString() {
        return "NoticeDTO{" +
                "noticeId=" + noticeId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", isEvent=" + isEvent +
                ", isFixed=" + isFixed +
                ", createdAt=" + createdAt +
                '}';
    }
}