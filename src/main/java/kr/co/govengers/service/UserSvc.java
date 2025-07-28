package kr.com.GoGiProject.service;

import kr.com.GoGiProject.entity.User;
import kr.com.GoGiProject.repository.UserRepo; // 이름을 UserRepo로 바꾸셨으니 맞춰서 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service // 이 클래스가 비즈니스 로직을 담당하는 서비스 클래스임을 나타냅니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다. (의존성 주입)
public class UserSvc {

    private final UserRepo userRepo; // 데이터베이스와 통신하기 위해 주입
    private final PasswordEncoder passwordEncoder; // 비밀번호를 비교하기 위해 주입

    public User login(String uid, String upw) {
        // 1. 아이디(uid)를 기반으로 DB에서 사용자 정보를 찾아온다.
        User user = userRepo.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디 입니다."));

        // 2. 입력된 비밀번호(upw)와 DB의 암호화된 비밀번호가 일치하는지 확인한다.
        if (!passwordEncoder.matches(upw, user.getUpw())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        // 3. 모든 인증이 성공하면, 해당 사용자 정보를 반환한다.
        return user;
    }

    public User join(User user) {
        // 1. 동일한 아이디(uid)를 가진 회원이 이미 있는지 확인
        if (userRepo.existsById(user.getUid())) {
            throw new IllegalArgumentException("이미 가입된 아이디 입니다.");
        }

        // 2. 비밀번호를 암호화
        String encodedPassword = passwordEncoder.encode(user.getUpw());
        user.setUpw(encodedPassword);

        // 3. 사용자 권한 설정 (기본값)
        user.setRole("ROLE_USER");

        // 4. 데이터베이스에 새로운 사용자 정보를 저장
        return userRepo.save(user);
    }

    // TODO: 회원가입(join) 메소드 등 다른 로직 추가 예정
}

