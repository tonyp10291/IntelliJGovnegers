package kr.co.govengers.controller;

import kr.co.govengers.entity.User;
import kr.co.govengers.service.UserSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/mypage/me")
@RequiredArgsConstructor
public class MeController {

    private final UserSvc userSvc;

    @GetMapping
    public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
        Map<String, Object> res = new HashMap<>();
        res.put("uid", user.getUid());
        res.put("unm", user.getUnm());
        res.put("utel", user.getUtel());
        res.put("umail", user.getUmail());
        return ResponseEntity.ok(res);
    }

    @PutMapping
    public ResponseEntity<?> update(@AuthenticationPrincipal User user,
                                    @RequestBody Map<String, Object> body) {
        Map<String, Object> update = new HashMap<>();
        if (body.containsKey("unm"))   update.put("unm",   body.get("unm"));
        if (body.containsKey("utel"))  update.put("utel",  body.get("utel"));
        if (body.containsKey("umail")) update.put("umail", body.get("umail"));

        User updated = userSvc.updateProfile(user.getUid(), update);

        Map<String, Object> res = new HashMap<>();
        res.put("uid", updated.getUid());
        res.put("unm", updated.getUnm());
        res.put("utel", updated.getUtel());
        res.put("umail", updated.getUmail());
        return ResponseEntity.ok(res);
    }

    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal User user,
                                            @RequestBody Map<String, String> body) {
        userSvc.changePassword(user.getUid(), body.get("currentPassword"), body.get("newPassword"));
        return ResponseEntity.ok().build();
    }
}