package kr.co.govengers.service;

import kr.co.govengers.entity.SmsAuth;
import kr.co.govengers.repository.SmsAuthRepo;
import kr.co.govengers.util.SmsUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SmsSvc {

    private final SmsUtil smsUtil;
    private final SmsAuthRepo smsAuthRepo;

    public void sendVerificationCode(String phone) {
        String code = String.format("%06d", new Random().nextInt(999999));
        String message = "[고벤저스] 인증번호: " + code;
        smsUtil.sendOne(phone, message);
        SmsAuth auth = new SmsAuth();
        auth.setPhone(phone);
        auth.setCode(code);
        auth.setCreatedTime(System.currentTimeMillis());
        smsAuthRepo.save(auth);
    }

    public boolean verifyCode(String phone, String code) {
        Optional<SmsAuth> optionalAuth = smsAuthRepo.findById(phone);

        if (optionalAuth.isPresent()) {
            SmsAuth auth = optionalAuth.get();
            if (System.currentTimeMillis() - auth.getCreatedTime() <= 3 * 60 * 1000 && auth.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}