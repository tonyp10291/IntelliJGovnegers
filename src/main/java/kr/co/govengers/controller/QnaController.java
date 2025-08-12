package kr.co.govengers.controller;

import kr.co.govengers.dto.QnaCommentDto;
import kr.co.govengers.dto.QnaDto;
import kr.co.govengers.dto.QnaSummaryDto;
import kr.co.govengers.dto.QnaWriteRequest;
import kr.co.govengers.entity.User;
import kr.co.govengers.service.QnaService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;

    @GetMapping
    public Page<QnaSummaryDto> list(@RequestParam Integer pid,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        return qnaService.list(pid, page, size);
    }

    @GetMapping("/{qid:\\d+}")
    public QnaDto detail(@PathVariable Long qid) {
        return qnaService.findById(qid);
    }

    @PostMapping("/{qid:\\d+}/verify")
    public Map<String, Object> verify(@PathVariable Long qid, @RequestBody VerifyReq req) {
        boolean ok = qnaService.verifyPassword(qid, req.getPassword());
        return Map.of("ok", ok);
    }
    @Data static class VerifyReq { private String password; }

    @PostMapping
    public ResponseEntity<?> write(@RequestBody QnaWriteRequest req, Authentication auth) {
        if (auth == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        qnaService.write(req, auth.getName());
        return ResponseEntity.ok().build();
    }



    @PostMapping("/{qid}/answer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> writeAnswer(@PathVariable Long qid,
                                         @RequestBody Map<String, String> body,
                                         Authentication auth) {
        String content = body.get("content");
        User admin = (User) auth.getPrincipal();
        qnaService.writeAnswer(qid, content, admin.getUid());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{qid}/comments")
    public List<QnaCommentDto> listComments(@PathVariable Long qid) {
        return qnaService.listComments(qid);
    }

    @DeleteMapping("/{qid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long qid, Authentication auth) {
        String uid = uidFromAuth(auth);
        qnaService.delete(qid, uid);
        return ResponseEntity.noContent().build();
    }

    private String uidFromAuth(Authentication auth) {
        if (auth == null) return null;
        Object p = auth.getPrincipal();
        if (p instanceof String s) return s;
        if (p instanceof User u) return u.getUid();
        return auth.getName();
    }

    @DeleteMapping("/{qid}/comments/{cid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteComment(@PathVariable Long qid, @PathVariable Long cid) {
        qnaService.deleteComment(qid, cid);
        return ResponseEntity.noContent().build();
    }
}
