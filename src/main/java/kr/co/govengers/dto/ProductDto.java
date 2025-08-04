package kr.co.govengers.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProductDto {
    private Integer pid;
    private String pnm;
    private String mainCategory;
    private String subCategory;
    private Integer price;
    private String pdesc;
    private String origin;
    private Integer soldout;
    private LocalDate expDate;
    private Integer hit;
    private String image;
}