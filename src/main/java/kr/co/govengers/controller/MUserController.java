package kr.co.govengers.controller;

import kr.co.govengers.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class MUserController {

    private final UserRepo userRepo;

    @GetMapping
    public ResponseEntity<?> getAllUser() {
        return ResponseEntity.ok(userRepo.findAll());
    }

    @GetMapping("/{uid}")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable String uid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        for(GrantedAuthority au : authentication.getAuthorities()){
            System.out.println("auth_role555: " + au.getAuthority());
        }

        return userRepo.findById(uid)
                .map(user -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("uid", user.getUid());
                    userInfo.put("unm", user.getUnm());
                    userInfo.put("utel", user.getUtel());
                    userInfo.put("point", user.getPoint());
                    userInfo.put("address", user.getAddress());
                    return ResponseEntity.ok(userInfo);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("msg", "사용자 정보를 찾을 수 없습니다.")));
    }
}
