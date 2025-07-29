package kr.co.govengers.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ProductRegisterRequest {
    private String name;
    private String category;
    private int price;
    private int stock;
    private String description;
    private MultipartFile image;
}
