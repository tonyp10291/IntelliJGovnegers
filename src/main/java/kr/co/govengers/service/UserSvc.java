package kr.co.govengers.service;

import kr.co.govengers.config.CustomUserDetails;
import kr.co.govengers.entity.User;
import kr.co.govengers.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSvc {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;


    public User login(String uid, String upw) {
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디 입니다."));
        if (!passwordEncoder.matches(upw, user.getUpw())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }
        return user;
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



    public User join(User user) {
        if (userRepo.existsById(user.getUid())) {
            throw new IllegalArgumentException("이미 가입된 아이디 입니다.");
        }
        String encodedPassword = passwordEncoder.encode(user.getUpw());
        user.setUpw(encodedPassword);
        user.setRole("ROLE_USER");
        return userRepo.save(user);
    }
}