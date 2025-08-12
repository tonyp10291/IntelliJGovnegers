package kr.co.govengers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter @Builder @AllArgsConstructor
public class QnaSummaryDto {
    private Long qid;
    private Integer pid;
    private String title;
    private String writerId;
    private LocalDateTime createdAt;
    private boolean secret;
    private int commentCount;
}