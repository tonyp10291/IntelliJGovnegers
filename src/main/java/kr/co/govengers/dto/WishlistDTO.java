package kr.co.govengers.dto;

import lombok.Data;

@Data
public class WishlistDTO {
    private Long id;
    private Integer pid;
    private String image;
    private String pnm;
    private Integer price;
    private int point;
    private int shippingCost;
    private int totalPrice;
}
