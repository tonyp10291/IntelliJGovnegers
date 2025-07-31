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

    // 모든 문의 목록 조회
    @Transactional(readOnly = true)
    public List<Inquiry> findAllInquiries() {
        return uqnARepo.findAll();
    }

    // 새로운 문의 등록
    @Transactional
    public Inquiry createInquiry(Inquiry inquiry, String userId) {
        // userId로 User 객체를 찾아서 문의(Inquiry)에 연결
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        inquiry.setUser(user);
        return uqnARepo.save(inquiry);
    }
}