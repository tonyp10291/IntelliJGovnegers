package kr.co.govengers.controller;

import kr.co.govengers.dto.NoticeDTO;
import kr.co.govengers.entity.Notice;
import kr.co.govengers.repository.NTRepo;
import kr.co.govengers.service.NTSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class NTController {

    private final NTRepo ntRepo;
    private final NTSvc ntSvc;

    @PostMapping
    public ResponseEntity<?> createNotice(@RequestBody NoticeDTO dto) {
        System.out.println("📥 공지 등록 컨트롤러 들어옴");
        if (dto.getTitle() == null || dto.getTitle().isEmpty() || dto.getContent() == null || dto.getContent().isEmpty()) {
            return ResponseEntity.badRequest().body("제목과 내용을 입력해주세요.");
        }

        if (dto.getTitle().length() > 50) {
            return ResponseEntity.badRequest().body("제목은 50자 이내여야 합니다.");
        }

        if (dto.getContent().length() > 1500) {
            return ResponseEntity.badRequest().body("내용은 1500자 이내여야 합니다.");
        }

        Notice notice = Notice.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .isEvent(dto.is_event())
                .isFixed(dto.is_fixed())
                .createdAt(LocalDateTime.now())
                .build();

        ntRepo.save(notice);

        return ResponseEntity.ok("등록 완료");
    }

    @GetMapping("/list")
    public List<Notice> getNoticeList() {
        return ntSvc.getAllNotices();
    }
}


