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
        this.isEvent = notice.isEvent();    // ⭐ Entity의 getter 호출
        this.isFixed = notice.isFixed();    // ⭐ Entity의 getter 호출
        this.createdAt = notice.getCreatedAt();

        // 디버깅용 로그 추가
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

    // ⭐ boolean 필드의 is() 형태 getter (Lombok 보완)
    public boolean isEvent() {
        return this.isEvent;
    }

    public boolean isFixed() {
        return this.isFixed;
    }

    // ⭐ JSON 직렬화를 위한 get() 형태 getter 추가
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