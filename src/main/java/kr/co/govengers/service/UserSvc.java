package kr.co.govengers.service;

import kr.co.govengers.entity.PswRsToken;
import kr.co.govengers.entity.User;
import kr.co.govengers.repository.PswRsTokenRepo;
import kr.co.govengers.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSvc {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final PswRsTokenRepo pswRsTokenRepo;
    private final EmailSvc emailSvc;

    @Transactional(readOnly = true)
    public User login(String uid, String upw) {
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디 입니다."));

        if (!passwordEncoder.matches(upw, user.getUpw())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        if (!user.isEnabled()) {
            throw new IllegalArgumentException("비활성화된 계정입니다.");
        }

        return user;
    }

    @Transactional
    public User join(User user) {
        if (userRepo.existsById(user.getUid())) {
            throw new IllegalArgumentException("이미 가입된 아이디 입니다.");
        }
        if (userRepo.existsByUmail(user.getUmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        user.setRole("ROLE_USER");
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setSmsVerified(true);
        user.setPoint(0);

        user.setUpw(passwordEncoder.encode(user.getUpw()));

        return userRepo.save(user);
    }

    @Transactional
    public User updateProfile(String uid, Map<String, Object> updateRequest) {
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (updateRequest.containsKey("unm")) {
            user.setUnm((String) updateRequest.get("unm"));
        }

        if (updateRequest.containsKey("umail")) {
            String newEmail = (String) updateRequest.get("umail");
            if (!user.getUmail().equals(newEmail) && userRepo.existsByUmail(newEmail)) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
            user.setUmail(newEmail);
            user.setEmailVerified(false);
        }

        if (updateRequest.containsKey("utel")) {
            user.setUtel((String) updateRequest.get("utel"));
            user.setSmsVerified(false);
        }

        if (updateRequest.containsKey("ubt")) {
            user.setUbt((String) updateRequest.get("ubt"));
        }

        return userRepo.save(user);
    }
//dds
    @Transactional
    public void changePassword(String uid, String currentPassword, String newPassword) {
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(currentPassword, user.getUpw())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setUpw(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    @Transactional
    public User updateUserPoints(String uid, int points) {
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        int newPoints = user.getPoint() + points;
        if (newPoints < 0) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }

        user.setPoint(newPoints);
        return userRepo.save(user);
    }

    @Transactional
    public User updateUserStatus(String uid, boolean enabled) {
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setEnabled(enabled);
        return userRepo.save(user);
    }

    @Transactional
    public void updateEmailVerification(String uid, boolean verified) {
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setEmailVerified(verified);
        userRepo.save(user);
    }

    @Transactional
    public void updateSmsVerification(String uid, boolean verified) {
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setSmsVerified(verified);
        userRepo.save(user);
    }

    @Transactional(readOnly = true)
    public String findId(String unm, String utel) {
        User user = userRepo.findByUnmAndUtel(unm, utel)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 사용자 정보가 없습니다."));
        return user.getUid();
    }

    @Transactional(readOnly = true)
    public String findIdByEmail(String unm, String umail) {
        User user = userRepo.findByUnmAndUmail(unm, umail)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 사용자 정보가 없습니다."));
        return user.getUid();
    }

    @Transactional
    public void createPasswordResetTokenAndSendEmail(String umail) {
        User user = userRepo.findByUmail(umail)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 사용자가 없습니다."));

        String token = UUID.randomUUID().toString();
        PswRsToken resetToken = new PswRsToken(token, user);
        pswRsTokenRepo.save(resetToken);

        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        try {
            emailSvc.sendPasswordResetLink(user.getUmail(), resetLink);
        } catch (Exception e) {
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PswRsToken resetToken = pswRsTokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 토큰입니다."));

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException("만료된 토큰입니다.");
        }

        User user = resetToken.getUser();
        user.setUpw(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        pswRsTokenRepo.delete(resetToken);
    }

    @Transactional(readOnly = true)
    public User getUserById(String uid) {
        return userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<User> getPagedUsers(Pageable pageable) {
        return userRepo.findByEnabledTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<User> searchUsersByKeyword(String keyword, Pageable pageable) {
        return userRepo.findByEnabledTrueAndUidContainingIgnoreCaseOrEnabledTrueAndUnmContainingIgnoreCase(keyword, keyword, pageable);
    }

    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return userRepo.countByEnabledTrue();
    }

    @Transactional(readOnly = true)
    public long getActiveUserCount() {
        return userRepo.countByEnabledTrue();
    }
}