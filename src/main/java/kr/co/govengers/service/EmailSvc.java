package kr.co.govengers.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kr.co.govengers.entity.EmailToken;
import kr.co.govengers.repository.EmailTokenRepo;
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
    private final EmailTokenRepo emailTokenRepo;

    @Transactional
    public void sendVerificationEmail(String email) throws MessagingException {
        String code = String.format("%06d", new Random().nextInt(999999));
        EmailToken token = EmailToken.builder()
                .email(email)
                .token(code)
                .isVerified(false)
                .expiryDate(LocalDateTime.now().plusMinutes(5))
                .build();
        emailTokenRepo.save(token);
        MimeMessage message = javaMailSender.createMimeMessage();
        message.setFrom("your-gmail@gmail.com");
        message.setRecipients(MimeMessage.RecipientType.TO, email);
        message.setSubject("[고벤저스] 이메일 인증번호");
        String body = "<h1>[고벤저스] 이메일 인증</h1><div>인증번호: <strong>" + code + "</strong></div>";
        message.setText(body, "UTF-8", "html");
        javaMailSender.send(message);
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        Optional<EmailToken> optionalToken = emailTokenRepo.findByEmailAndToken(email, code);

        if (optionalToken.isPresent()) {
            EmailToken token = optionalToken.get();
            if (!token.isVerified() && token.getExpiryDate().isAfter(LocalDateTime.now())) {
                token.setVerified(true);
                emailTokenRepo.save(token);
                return true;
            }
        }
        return false;
    }
}