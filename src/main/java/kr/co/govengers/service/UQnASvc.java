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
    public List<Inquiry> findAllInquiries() {
        return uqnARepo.findAll();
    }

    @Transactional
    public Inquiry createInquiry(Inquiry inquiry, String userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        inquiry.setUser(user);
        return uqnARepo.save(inquiry);
    }
}