package kr.co.govengers.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class ProductRegisterRequest {
    private String pnm;
    private String mainCategory;
    private String subCategory;
    private Integer price;
    private String pdesc;
    private String origin;
    private LocalDate expDate;
    private Integer hit;
    private String userStatus;
    private String adminStatus;
    private MultipartFile image;
}