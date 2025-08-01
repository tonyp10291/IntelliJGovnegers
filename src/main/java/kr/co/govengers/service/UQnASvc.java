package kr.co.govengers.service;

import kr.co.govengers.entity.Inquiry;
import kr.co.govengers.entity.User;
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
    public List<Inquiry> findInquiriesForUser(String category, String keyword, String userId) {
        if (category != null && !category.equals("전체") && keyword != null && !keyword.isBlank()) {
            return uqnARepo.findByCategoryAndTitleContainingAndVisibilityForUser(category, keyword, userId);
        }
        else if (category != null && !category.equals("전체")) {
            return uqnARepo.findByCategoryAndVisibilityForUser(category, userId);
        }
        else if (keyword != null && !keyword.isBlank()) {
            return uqnARepo.findByTitleContainingAndVisibilityForUser(keyword, userId);
        }
        else {
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
}