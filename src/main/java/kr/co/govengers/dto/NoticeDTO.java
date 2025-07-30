package kr.co.govengers.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeDTO {
    private String title;
    private String content;
    private boolean is_event;
    private boolean is_fixed;
}
