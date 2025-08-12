package kr.co.govengers.service;

import kr.co.govengers.entity.Inquiry;
import kr.co.govengers.entity.User;
import kr.co.govengers.entity.enums.InquiryCategory;
import kr.co.govengers.repository.UQnARepo;
import kr.co.govengers.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UQnASvc {

    private final UQnARepo uqnARepo;
    private final UserRepo userRepo;

    @Transactional(readOnly = true)
    public List<Inquiry> findInquiriesByUid(String uid, String category, String keyword) {
        if (category != null && !category.equals("전체") && keyword != null && !keyword.isBlank()) {
            InquiryCategory inquiryCategory = InquiryCategory.fromString(category);
            return uqnARepo.findByUserUidAndCategoryAndTitleContaining(uid, inquiryCategory, keyword);
        } else if (category != null && !category.equals("전체")) {
            InquiryCategory inquiryCategory = InquiryCategory.fromString(category);
            return uqnARepo.findByUserUidAndCategory(uid, inquiryCategory);
        } else if (keyword != null && !keyword.isBlank()) {
            return uqnARepo.findByUserUidAndTitleContaining(uid, keyword);
        } else {
            return uqnARepo.findByUserUidOrderByCreatedAtDesc(uid);
        }
    }

    @Transactional(readOnly = true)
    public List<Inquiry> findInquiriesForUser(String category, String keyword, String userId) {
        if (category != null && !category.equals("전체") && keyword != null && !keyword.isBlank()) {
            InquiryCategory inquiryCategory = InquiryCategory.fromString(category);
            return uqnARepo.findByCategoryAndTitleOrUserUidContainingAndVisibilityForUser(inquiryCategory, keyword, userId);
        } else if (category != null && !category.equals("전체")) {
            InquiryCategory inquiryCategory = InquiryCategory.fromString(category);
            return uqnARepo.findByCategoryAndVisibilityForUser(inquiryCategory, userId);
        } else if (keyword != null && !keyword.isBlank()) {
            return uqnARepo.findByTitleOrUserUidContainingAndVisibilityForUser(keyword, userId);
        } else {
            return uqnARepo.findAllWithVisibilityForUser(userId);
        }
    }

    @Transactional(readOnly = true)
    public List<Inquiry> findAllInquiries() {
        return uqnARepo.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Inquiry createInquiry(Inquiry inquiry, String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        inquiry.setUser(user);
        return uqnARepo.save(inquiry);
    }

    @Transactional
    public Inquiry updateInquiry(Long inquiryId, Inquiry updatedInquiry, String userId) {
        Inquiry existingInquiry = uqnARepo.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        if (existingInquiry.getUser() == null || !existingInquiry.getUser().getUid().equals(userId)) {
            throw new SecurityException("이 문의를 수정할 권한이 없습니다.");
        }
        if (existingInquiry.getAnswer() != null && !existingInquiry.getAnswer().isBlank()) {
            throw new IllegalStateException("답변이 달린 문의는 수정할 수 없습니다.");
        }

        existingInquiry.setTitle(updatedInquiry.getTitle());
        existingInquiry.setContent(updatedInquiry.getContent());
        existingInquiry.setCategory(updatedInquiry.getCategory());
        existingInquiry.setIsPrivate(updatedInquiry.getIsPrivate());

        return uqnARepo.save(existingInquiry);
    }

    @Transactional
    public void deleteInquiry(Long inquiryId, String userId) {
        Inquiry inquiry = uqnARepo.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        if (inquiry.getUser() == null || !inquiry.getUser().getUid().equals(userId)) {
            throw new SecurityException("이 문의를 삭제할 권한이 없습니다.");
        }
        if (inquiry.getAnswer() != null && !inquiry.getAnswer().isBlank()) {
            throw new IllegalStateException("답변이 달린 문의는 삭제할 수 없습니다.");
        }
        uqnARepo.delete(inquiry);
    }

    @Transactional(readOnly = true)
    public Inquiry getInquiryForUser(Long inquiryId, String userId) {
        Inquiry inquiry = uqnARepo.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다."));

        if (inquiry.getIsPrivate() &&
                (userId == null || inquiry.getUser() == null ||
                        !inquiry.getUser().getUid().equals(userId))) {
            throw new SecurityException("이 문의를 볼 권한이 없습니다.");
        }
        return inquiry;
    }

    @Transactional(readOnly = true)
    public long countInquiriesByUser(String userId) {
        return uqnARepo.countByUserUid(userId);
    }

    @Transactional(readOnly = true)
    public List<Inquiry> findPendingInquiries() {
        return uqnARepo.findByAnswerIsNullOrAnswerOrderByCreatedAtAsc("");
    }
}