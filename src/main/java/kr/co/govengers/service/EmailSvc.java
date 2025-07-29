//추가
package kr.co.govengers.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kr.co.govengers.entity.EmailToken;
import kr.co.govengers.repository.EmailTokenRepo; // EmailTokenRepo로 수정
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailSvc {

    private final JavaMailSender javaMailSender;
    private final EmailTokenRepo emailTokenRepo; // Repo 주입

    @Transactional
    public void sendVerificationEmail(String email) throws MessagingException {
        String code = String.format("%06d", new Random().nextInt(999999));

        // DB에 저장할 토큰 엔티티 생성
        EmailToken token = EmailToken.builder()
                .email(email)
                .token(code)
                .isVerified(false)
                .expiryDate(LocalDateTime.now().plusMinutes(5)) // 유효시간 5분
                .build();
        emailTokenRepo.save(token); // DB에 저장

        // 이메일 발송 로직 (기존과 동일)
        MimeMessage message = javaMailSender.createMimeMessage();
        message.setFrom("idelleene@gmail.com"); // ← spring.mail.username과 반드시 같아야 함
        message.setRecipients(MimeMessage.RecipientType.TO, email); // 사용자 이메일 (유동적)
        message.setSubject("[고벤저스] 이메일 인증번호");

        String body = "<h1>[고벤저스] 이메일 인증</h1><div>인증번호: <strong>" + code + "</strong></div>";
        message.setText(body, "UTF-8", "html");
        javaMailSender.send(message);
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        // DB에서 이메일과 코드로 토큰 정보 조회
        Optional<EmailToken> optionalToken = emailTokenRepo.findByEmailAndToken(email, code);

        if (optionalToken.isPresent()) {
            EmailToken token = optionalToken.get();
            // 토큰이 만료되지 않았고, 아직 인증되지 않았다면
            if (!token.isVerified() && token.getExpiryDate().isAfter(LocalDateTime.now())) {
                token.setVerified(true); // 인증 상태로 변경
                emailTokenRepo.save(token);
                return true;
            }
        }
        return false;
    }
}