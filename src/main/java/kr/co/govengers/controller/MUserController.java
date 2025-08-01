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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")  // ← 복수형으로 통일
@RequiredArgsConstructor
public class MUserController {

    private final UserSvc userSvc;
    private final UserRepo userRepo;

    // ✅ 사용자 목록 페이징 조회
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

    // ✅ 사용자 상세 정보 조회 (권한 출력 로그 포함)
    @GetMapping("/{uid}")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable String uid) {
        // ⬇️ 현재 인증된 사용자 권한 출력 (테스트용 로그)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        for (GrantedAuthority au : authentication.getAuthorities()) {
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
                        .body(Map.of("msg", "사용자 정보를 찾을 수 없습니다.")));  // ← key는 "msg"로 통일
    }
}
