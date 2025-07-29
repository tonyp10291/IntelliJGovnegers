    package kr.co.govengers.service;

    import kr.co.govengers.entity.User;
    import kr.co.govengers.repository.UserRepo;
    import lombok.RequiredArgsConstructor;
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

