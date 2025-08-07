package kr.co.govengers.dto;

import kr.co.govengers.entity.enums.AdminStatus;
import kr.co.govengers.entity.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProductDTO {
    private Integer pid;
    private String pnm;
    private String mainCategory;
    private Integer price;
    private String pdesc;
    private String origin;
    private Integer soldout;
    private LocalDate expDate;
    private Integer hit;
    private String image;
    private AdminStatus adminStatus;
    private UserStatus userStatus;
}

