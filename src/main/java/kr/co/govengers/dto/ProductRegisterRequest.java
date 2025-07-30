package kr.co.govengers.dto;

import lombok.Data;

@Data
public class ProductRegisterRequest {
    private String name;
    private String mainCategory;
    private int price;
    private String description;
    private int stock;
    private int hit; // hit(0/1)
    // 이미지 등 추가필요시 필드 확장
}
