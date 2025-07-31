package kr.co.govengers.dto;

import lombok.Data;

@Data
public class ProductRegisterRequest {
    private String name;
    private String mainCategory;
    private String subCategory;
    private int price;
    private String description;
    private int stock;
    private int hit;
}