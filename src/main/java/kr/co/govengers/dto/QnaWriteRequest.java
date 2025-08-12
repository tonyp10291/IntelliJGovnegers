package kr.co.govengers.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QnaWriteRequest {
    private Integer pid;
    private String title;
    private String content;
    private boolean secret;
    private String password;
}
