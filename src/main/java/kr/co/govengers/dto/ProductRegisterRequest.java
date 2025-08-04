package kr.co.govengers.dto;

import kr.co.govengers.entity.enums.AdminStatus;
import kr.co.govengers.entity.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProductRegisterRequest {
    private String pnm;
    private String mainCategory;
    private Integer price;
    private String pdesc;
    private String origin;
    private String expDate;
    private Integer hit;
    private Integer soldout;
    private String userStatus;
    private String adminStatus;
    private MultipartFile image;
}