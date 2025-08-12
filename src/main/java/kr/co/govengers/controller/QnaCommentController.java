package kr.co.govengers.controller;

import kr.co.govengers.service.QnaService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/qna")
@RequiredArgsConstructor
public class QnaCommentController {

    private final QnaService qnaService;

    @PostMapping("/{qid}/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addComment(@PathVariable Long qid,
                                        @RequestBody CommentReq req,
                                        Authentication auth) {
        qnaService.addAdminComment(qid, req.getContent(), auth.getName());
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @Getter
    @Setter
    public static class CommentReq {
        private String content;
    }
}
