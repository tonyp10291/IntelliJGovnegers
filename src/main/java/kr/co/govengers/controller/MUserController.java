package kr.co.govengers.controller;

import kr.co.govengers.entity.User;
import kr.co.govengers.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class MUserController {

    private final UserRepo UserRepo;

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(UserRepo.findAll());
    }

    @GetMapping("/{uid}")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable String uid) {
        return UserRepo.findById(uid)
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
