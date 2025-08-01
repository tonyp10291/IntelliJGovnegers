package kr.co.govengers.controller;

import kr.co.govengers.entity.User;
import kr.co.govengers.repository.UserRepo;
import kr.co.govengers.service.UserSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class MUserController {

    private final UserSvc userSvc;
    private final UserRepo userRepo;

    @GetMapping
    public Page<User> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword
    ) {
        Pageable pageable = PageRequest.of(page, 10);

        if (keyword != null && !keyword.isBlank()) {
            return userSvc.searchUsersByKeyword(keyword, pageable);
        }

        return userSvc.getPagedUsers(pageable);
    }

    @GetMapping("/{uid}")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable String uid) {
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
