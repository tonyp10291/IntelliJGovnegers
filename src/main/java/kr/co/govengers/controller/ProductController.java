package kr.co.govengers.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public ResponseEntity<?> plist(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        for(GrantedAuthority au : authentication.getAuthorities()){
            System.out.println("/api/products  auth_role: " + au.getAuthority());
        }
        Map<String, String> response1 = Map.of(
                "imageUrl", "",
                "name", "상품명1",
                "price" , "3,000원",
                "soldOut" , "중지",
                "hit", "hit1",
                "new" , "new상품1"
        );
        Map<String, String> response2 = Map.of(
                "imageUrl", "",
                "name", "상품명2",
                "price" , "7,000원",
                "soldOut" , "판매",
                "hit", "hit2",
                "new" , "new상품2"
        );
        ArrayList<Map<String, String>> response = new ArrayList<>();
        response.add(response1);
        response.add(response2);
        return ResponseEntity.ok(response);
    }
}
