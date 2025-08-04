package kr.co.govengers.controller;

import kr.co.govengers.dto.NoticeDTO;
import kr.co.govengers.entity.Notice;
import kr.co.govengers.repository.NTRepo;
import kr.co.govengers.service.NTSvc;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
public class NTController {

    private final NTRepo ntRepo;

    @GetMapping("/api/notices/list")
    public ResponseEntity<List<NoticeDTO>> getNotices() {
        try {
            List<Notice> notices = ntRepo.findAllByOrderByIsFixedDescCreatedAtDesc();
            System.out.println("📋 조회된 공지 수: " + notices.size());

            List<NoticeDTO> noticeDTOs = notices.stream()
                    .map(NoticeDTO::new)
                    .collect(Collectors.toList());

            System.out.println("✅ DTO 변환 완료, 개수: " + noticeDTOs.size());

            System.out.println("🔍 JSON으로 전송할 데이터:");
            noticeDTOs.forEach(dto -> {
                System.out.println("ID: " + dto.getNoticeId() +
                        " | isEvent: " + dto.getIsEvent() +
                        " | isFixed: " + dto.getIsFixed());
            });

            return ResponseEntity.ok(noticeDTOs);

        } catch (Exception e) {
            System.err.println("❌ 공지사항 조회 중 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/api/notices/view/{id}")
    public ResponseEntity<?> getNotice(@PathVariable Long id) {
        System.out.println("📋 공지 상세 조회 - ID: " + id);

        try {
            Notice notice = ntRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

            System.out.println("✅ 공지 상세 조회 성공:");
            System.out.println("   - title: " + notice.getTitle());
            System.out.println("   - content: " + notice.getContent());
            System.out.println("   - isEvent: " + notice.isEvent());
            System.out.println("   - isFixed: " + notice.isFixed());
            System.out.println("   - createdAt: " + notice.getCreatedAt());

            NoticeDTO dto = new NoticeDTO(notice);
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            System.out.println("❌ 공지를 찾을 수 없음 - ID: " + id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/admin/notices/view/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getNoticeForAdmin(@PathVariable Long id) {
        System.out.println("📋 [관리자] 공지 상세 조회 - ID: " + id);

        try {
            Notice notice = ntRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

            System.out.println("✅ [관리자] 공지 상세 조회 성공:");
            System.out.println("   - title: " + notice.getTitle());
            System.out.println("   - content: " + notice.getContent());
            System.out.println("   - isEvent: " + notice.isEvent());
            System.out.println("   - isFixed: " + notice.isFixed());
            System.out.println("   - createdAt: " + notice.getCreatedAt());

            NoticeDTO dto = new NoticeDTO(notice);
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            System.out.println("❌ [관리자] 공지를 찾을 수 없음 - ID: " + id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/api/notices")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createNotice(@RequestBody NoticeDTO dto) {
        System.out.println("📥 [관리자] 공지 등록 컨트롤러 들어옴");
        System.out.println("📥 받은 DTO: " + dto);
        System.out.println("🧪 title: " + dto.getTitle());
        System.out.println("🧪 content: " + dto.getContent());
        System.out.println("🧪 isEvent: " + dto.isEvent());
        System.out.println("🧪 isFixed: " + dto.isFixed());

        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("제목을 입력해주세요.");
        }

        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("내용을 입력해주세요.");
        }

        if (dto.getTitle().length() > 50) {
            return ResponseEntity.badRequest().body("제목은 50자 이내여야 합니다.");
        }

        if (dto.getContent().length() > 1500) {
            return ResponseEntity.badRequest().body("내용은 1500자 이내여야 합니다.");
        }

        try {
            Notice entity = dto.toEntity();
            System.out.println("🏗️ 생성된 Entity:");
            System.out.println("   - title: " + entity.getTitle());
            System.out.println("   - content: " + entity.getContent());
            System.out.println("   - isEvent: " + entity.isEvent());
            System.out.println("   - isFixed: " + entity.isFixed());

            Notice savedNotice = ntRepo.save(entity);
            System.out.println("💾 저장된 Entity:");
            System.out.println("   - noticeId: " + savedNotice.getNoticeId());
            System.out.println("   - title: " + savedNotice.getTitle());
            System.out.println("   - isEvent: " + savedNotice.isEvent());
            System.out.println("   - isFixed: " + savedNotice.isFixed());

            return ResponseEntity.ok(new NoticeDTO(savedNotice));

        } catch (Exception e) {
            System.err.println("❌ [관리자] 공지사항 생성 중 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("공지사항 등록에 실패했습니다.");
        }
    }

    @GetMapping("/api/notices/edit/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getNoticeForEdit(@PathVariable Long id) {
        System.out.println("📝 [관리자] 수정용 공지 조회 - ID: " + id);

        try {
            Notice notice = ntRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

            System.out.println("✅ [관리자] 수정용 공지 조회 성공: " + notice.getTitle());

            NoticeDTO dto = new NoticeDTO(notice);
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            System.out.println("❌ [관리자] 수정용 공지를 찾을 수 없음 - ID: " + id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/api/notices/update/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> updateNotice(@PathVariable Long id, @RequestBody NoticeDTO dto) {
        System.out.println("📝 [관리자] 공지 수정 컨트롤러 들어옴 - ID: " + id);
        System.out.println("📝 받은 DTO: " + dto);

        try {
            Notice existingNotice = ntRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("수정할 공지사항을 찾을 수 없습니다."));

            if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("제목을 입력해주세요.");
            }

            if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("내용을 입력해주세요.");
            }

            if (dto.getTitle().length() > 50) {
                return ResponseEntity.badRequest().body("제목은 50자 이내여야 합니다.");
            }

            if (dto.getContent().length() > 1500) {
                return ResponseEntity.badRequest().body("내용은 1500자 이내여야 합니다.");
            }

            existingNotice.setTitle(dto.getTitle());
            existingNotice.setContent(dto.getContent());
            existingNotice.setEvent(dto.isEvent());
            existingNotice.setFixed(dto.isFixed());

            System.out.println("🔄 수정할 Entity:");
            System.out.println("   - noticeId: " + existingNotice.getNoticeId());
            System.out.println("   - title: " + existingNotice.getTitle());
            System.out.println("   - isEvent: " + existingNotice.isEvent());
            System.out.println("   - isFixed: " + existingNotice.isFixed());

            Notice updatedNotice = ntRepo.save(existingNotice);

            System.out.println("✅ [관리자] 수정 완료: " + updatedNotice.getTitle());

            return ResponseEntity.ok(new NoticeDTO(updatedNotice));

        } catch (Exception e) {
            System.err.println("❌ [관리자] 공지사항 수정 중 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("공지사항 수정에 실패했습니다.");
        }
    }

    @DeleteMapping("/api/notices/delete/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> deleteNotice(@PathVariable Long id) {
        System.out.println("🗑️ [관리자] 공지 삭제 컨트롤러 들어옴 - ID: " + id);

        try {
            Notice existingNotice = ntRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("삭제할 공지사항을 찾을 수 없습니다."));

            System.out.println("🗑️ 삭제할 공지: " + existingNotice.getTitle());

            ntRepo.delete(existingNotice);

            System.out.println("✅ [관리자] 삭제 완료 - ID: " + id);

            return ResponseEntity.ok("공지사항이 삭제되었습니다.");

        } catch (Exception e) {
            System.err.println("❌ [관리자] 공지사항 삭제 중 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("공지사항 삭제에 실패했습니다.");
        }
    }
}