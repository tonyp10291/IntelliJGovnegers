package kr.co.govengers.config;

import kr.co.govengers.entity.User;
import kr.co.govengers.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInit {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initAdminData() {
        return args -> {
            // "admin" 아이디를 가진 사용자가 없을 때만 아래 로직을 실행
            if (!userRepo.existsById("admin")) {
                User admin = new User();
                admin.setUid("admin");
                admin.setUpw(passwordEncoder.encode("1111"));
                admin.setUnm("관리자");
                admin.setUmail("admin@gogi.com");
                admin.setRole("ROLE_ADMIN");
                admin.setEnabled(true);
                admin.setEmailVerified(true);
                admin.setSmsVerified(true);

                userRepo.save(admin);
                System.out.println("✅ 초기 관리자 계정이 생성되었습니다.");
            }
        };
    }
}