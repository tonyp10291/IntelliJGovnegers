package kr.co.govengers.service;

import kr.co.govengers.config.CustomUserDetails;
import kr.co.govengers.entity.PswRsToken;
import kr.co.govengers.entity.User;
import kr.co.govengers.repository.PswRsTokenRepo;
import kr.co.govengers.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        user.setUpw(passwordEncoder.encode(user.getUpw()));
        return userRepo.save(user);
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

    //20250731_영미:
    public UserDetails checkUser(String uid, String upw) throws IllegalArgumentException {
        System.out.println("CustomUserDetailsService loadUserByUserId 실행");
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디 입니다."));
        if (!passwordEncoder.matches(upw, user.getUpw())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        };
        return new CustomUserDetails(user);
    }
    //20250731_영미:
    public UserDetails checkUser(String uid) throws IllegalArgumentException {
        System.out.println("CustomUserDetailsService loadUserByUserId 실행");
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디 입니다."));
        return new CustomUserDetails(user);
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
    public Page<User> getPagedUsers(Pageable pageable) {
        return userRepo.findByEnabledTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<User> searchUsersByKeyword(String keyword, Pageable pageable) {
        return userRepo.findByEnabledTrueAndUidContainingIgnoreCaseOrEnabledTrueAndUnmContainingIgnoreCase(keyword, keyword, pageable);
    }
}