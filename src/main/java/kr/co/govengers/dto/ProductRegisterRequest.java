package kr.co.govengers.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProductRegisterRequest {
    private String pnm;
    private String mainCategory;
    private String subCategory;
    private Integer price;
    private String pdesc;
    private String origin;
    private String expDate;  // 프론트에서 date 타입으로 전송시 String or LocalDate도 가능
    private Integer hit;
    private Integer soldout;
    private String userStatus;
    private String adminStatus;
    private MultipartFile image;   // ★ 반드시 필요!
}