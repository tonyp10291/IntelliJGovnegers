package kr.co.govengers.dto;

import kr.co.govengers.entity.Cart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {
    private Integer cartId;
    private Integer productId;
    private String productName;
    private Integer price;
    private Integer quantity;
    private String imageFilename;
    private String memo;
    private Integer shippingCost;
    private Integer point;

    public static CartItemDTO from(Cart cart) {
        return CartItemDTO.builder()
                .cartId(cart.getCartId())
                .productId(cart.getProduct().getPid())
                .productName(cart.getProduct().getPnm())
                .price(cart.getProduct().getPrice())
                .quantity(cart.getQuantity())
                .imageFilename(cart.getProduct().getImage())
                .memo(cart.getMemo())
                .shippingCost(cart.getProduct().shippingCost())
                .point(cart.getProduct().calculatePoint())
                .build();
    }
}