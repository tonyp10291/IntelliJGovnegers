package kr.co.govengers.service;

import kr.co.govengers.entity.Inquiry;
import kr.co.govengers.entity.enums.InquiryCategory;
import kr.co.govengers.repository.MQnARepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MQnASvc {

    private final MQnARepo mqnaRepo;

    // 전체 문의 목록 조회 (페이징)
    public Page<Inquiry> getPagedInquiries(Pageable pageable) {
        return mqnaRepo.findAllByOrderByCreatedAtDesc(pageable);
    }

    // 검색 기능
    public Page<Inquiry> searchInquiriesByKeyword(String keyword, Pageable pageable) {
        return mqnaRepo.findByKeywordOrderByCreatedAtDesc(keyword, pageable);
    }

    // 카테고리별 조회
    public Page<Inquiry> getInquiriesByCategory(String category, Pageable pageable) {
        try {
            InquiryCategory inquiryCategory = InquiryCategory.valueOf(category);
            return mqnaRepo.findByCategoryOrderByCreatedAtDesc(inquiryCategory, pageable);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("잘못된 카테고리입니다: " + category);
        }
    }

    // 답변 상태별 조회
    public Page<Inquiry> getInquiriesByAnswerStatus(String answerStatus, Pageable pageable) {
        if (!"PENDING".equals(answerStatus) && !"ANSWERED".equals(answerStatus)) {
            throw new IllegalArgumentException("잘못된 답변 상태입니다: " + answerStatus);
        }
        return mqnaRepo.findByAnswerStatusOrderByCreatedAtDesc(answerStatus, pageable);
    }

    // 공개/비공개별 조회
    public Page<Inquiry> getInquiriesByPrivacy(boolean isPrivate, Pageable pageable) {
        return mqnaRepo.findByIsPrivateOrderByCreatedAtDesc(isPrivate, pageable);
    }

    // 개별 문의 조회
    public Inquiry findById(Long inquiryId) {
        return mqnaRepo.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));
    }

    // 관리자 답변 등록/수정
    @Transactional
    public Inquiry addAnswer(Long inquiryId, String answer, String adminId) {
        Inquiry inquiry = findById(inquiryId);
        inquiry.setAnswer(answer);
        inquiry.setAnswerAt(LocalDateTime.now());
        // 추가적으로 adminId를 저장하는 필드가 있다면 설정
        return mqnaRepo.save(inquiry);
    }

    // 문의 삭제 (관리자 권한)
    @Transactional
    public void deleteInquiry(Long inquiryId) {
        if (!mqnaRepo.existsById(inquiryId)) {
            throw new IllegalArgumentException("문의를 찾을 수 없습니다.");
        }
        mqnaRepo.deleteById(inquiryId);
    }

    // 통계 정보들
    public long getTotalCount() {
        return mqnaRepo.count();
    }

    public long getPendingCount() {
        return mqnaRepo.countPending();
    }

    public long getAnsweredCount() {
        return mqnaRepo.countAnswered();
    }

    public long getPrivateCount() {
        return mqnaRepo.countByIsPrivateTrue();
    }
}