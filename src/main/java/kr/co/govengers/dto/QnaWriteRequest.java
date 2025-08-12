package kr.co.govengers.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QnaWriteRequest {
    private Integer pid;       // 상품 ID
    private String title;
    private String content;
    private boolean secret;
    private String password;   // 비밀글일 경우
}
