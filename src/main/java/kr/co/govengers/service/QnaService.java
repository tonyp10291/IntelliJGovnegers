package kr.co.govengers.service;

import kr.co.govengers.dto.QnaCommentDto;
import kr.co.govengers.dto.QnaDto;
import kr.co.govengers.dto.QnaSummaryDto;
import kr.co.govengers.dto.QnaWriteRequest;
import kr.co.govengers.entity.Qna;
import kr.co.govengers.entity.QnaComment;
import kr.co.govengers.entity.User;
import kr.co.govengers.repository.QnaCommentRepository;
import kr.co.govengers.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QnaService {

    private final QnaRepository qnaRepository;
    private final QnaCommentRepository qnaCommentRepository;

    @Transactional(readOnly = true)
    public List<QnaDto> getAllDtos() {
        return qnaRepository.findAll().stream().map(QnaDto::new).toList();
    }

    @Transactional(readOnly = true)
    public Page<QnaSummaryDto> list(Integer pid, int page, int size) {
        int pageIndex = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "qid"));

        return qnaRepository.findByPid(pid, pageable)
                .map(q -> QnaSummaryDto.builder()
                        .qid(q.getQid())
                        .pid(q.getPid())
                        .title(q.getTitle())
                        .writerId(q.getWriterId())
                        .createdAt(q.getCreatedAt())
                        .secret(q.isSecret())
                        .commentCount(q.getComments() == null ? 0 : q.getComments().size())
                        .build());
    }

    @Transactional
    public void write(QnaWriteRequest req, String writerId) {
        Qna q = Qna.builder()
                .pid(req.getPid())
                .title(req.getTitle())
                .content(req.getContent())
                .secret(req.isSecret())
                .password(req.isSecret() ? req.getPassword() : null)
                .writerId(writerId)
                .createdAt(LocalDateTime.now())
                .build();
        qnaRepository.save(q);
    }

    @Transactional(readOnly = true)
    public QnaDto findById(Long qid) {
        Qna q = qnaRepository.findById(qid)
                .orElseThrow(() -> new IllegalArgumentException("QnA가 존재하지 않습니다. qid=" + qid));
        return new QnaDto(q);
    }

    @Transactional(readOnly = true)
    public boolean verifyPassword(Long qid, String rawPassword) {
        Qna q = qnaRepository.findById(qid)
                .orElseThrow(() -> new IllegalArgumentException("QnA가 존재하지 않습니다. qid=" + qid));
        if (!q.isSecret()) return true;
        return rawPassword != null && rawPassword.equals(q.getPassword()); // 암호화 시 encoder.matches
    }

    @Transactional(readOnly = true)
    public List<QnaCommentDto> listComments(Long qid) {
        return qnaCommentRepository.findByQna_QidOrderByCreatedAtAsc(qid)
                .stream().map(QnaCommentDto::new).toList();
    }

    @Transactional
    public Long addAdminComment(Long qid, String content, String adminUidMaybeName) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("댓글 내용을 입력하세요.");
        }

        boolean isAdmin = false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            isAdmin = auth.getAuthorities().stream().anyMatch(a -> {
                String r = a.getAuthority();
                return "ROLE_ADMIN".equalsIgnoreCase(r) || "ADMIN".equalsIgnoreCase(r);
            });
        }
        if (!isAdmin) throw new SecurityException("관리자만 댓글을 등록할 수 있습니다.");

        String adminUid = adminUidMaybeName;
        if (auth != null && (adminUid == null || adminUid.isBlank())) {
            Object principal = auth.getPrincipal();
            if (principal instanceof User u) adminUid = u.getUid();
        }

        Qna qna = qnaRepository.findById(qid)
                .orElseThrow(() -> new IllegalArgumentException("QnA가 존재하지 않습니다. qid=" + qid));

        QnaComment c = QnaComment.builder()
                .qna(qna)
                .content(content)
                .writerId(adminUid)                 // 관리자 uid
                .createdAt(LocalDateTime.now())
                .build();

        if (qna.getComments() != null) qna.getComments().add(c); // 양방향일 때 안전 추가
        QnaComment saved = qnaCommentRepository.save(c);

        return saved.getCid();
    }

    @Transactional
    public QnaCommentDto addAdminCommentReturnDto(Long qid, String content, String adminUid) {
        Long cid = addAdminComment(qid, content, adminUid);
        QnaComment saved = qnaCommentRepository.findById(cid)
                .orElseThrow(() -> new IllegalStateException("댓글 저장 후 조회 실패: cid=" + cid));
        return new QnaCommentDto(saved);
    }

    @Transactional
    public void deleteComment(Long qid, Long cid) {
        QnaComment c = qnaCommentRepository.findById(cid)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다. cid=" + cid));
        if (c.getQna() == null || !qid.equals(c.getQna().getQid())) {
            throw new IllegalArgumentException("해당 QnA의 댓글이 아닙니다.");
        }
        qnaCommentRepository.delete(c);
    }

    /** 호환용: writeAnswer 이름을 쓰는 기존 코드가 있으면 이쪽으로 위임 */
    @Transactional
    public Long writeAnswer(Long qid, String content, String adminUid) {
        return addAdminComment(qid, content, adminUid);
    }

    @Transactional
    public void delete(Long qid, String requesterUid) {
        Qna q = qnaRepository.findById(qid)
                .orElseThrow(() -> new IllegalArgumentException("QnA가 존재하지 않습니다."));

        boolean isAdmin = false;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            isAdmin = auth.getAuthorities().stream().anyMatch(a -> {
                String r = a.getAuthority();
                return "ROLE_ADMIN".equalsIgnoreCase(r) || "ADMIN".equalsIgnoreCase(r);
            });
        }
        if (!isAdmin && (requesterUid == null || !requesterUid.equals(q.getWriterId()))) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }
        qnaRepository.delete(q);
    }

}
