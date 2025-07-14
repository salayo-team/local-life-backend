package com.salayo.locallifebackend.domain.email.service;

import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import java.time.Duration;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;

    public EmailService(JavaMailSender mailSender,
        @Qualifier("emailVerifiedRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate;
    }

    private static final long EXPIRE_TIME = 5 * 60L;

    public void sendVerificationCode(String email) {
        String code = generateCode();

        clearVerifiedFlag(email);

        redisTemplate.opsForValue().set("email_code:" + email, code, Duration.ofSeconds(EXPIRE_TIME));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[LocalLife] 이메일 인증 코드입니다.");
        message.setText("인증코드: " + code);

        mailSender.send(message);
        log.info("이메일 전송 완료: {} / 코드: {}", email, code);
    }

    private String generateCode() {
        return String.valueOf(new Random().nextInt(900_000) + 100_000);
    }

    public void clearVerifiedFlag(String email) {
        redisTemplate.delete("email_verified: " + email);
    }

    public void verifyEmailCode(String email, String code) {
        String key = "email_code:" + email;
        String savedCode = redisTemplate.opsForValue().get(key);

        if (savedCode == null) {
            throw new CustomException(ErrorCode.EMAIL_CODE_EXPIRED);
        }

        if (!savedCode.equals(code)) {
            throw new CustomException(ErrorCode.INVALID_EMAIL_CODE);
        }

        /**
         * 인증 성공 처리
         * -인증 완료 플래그(email_verified:{email})를 Redis에 30분 동안 저장
         * -인증 코드는 재사용 방지를 위해 즉시 삭제
         */
        redisTemplate.opsForValue().set("email_verified:" + email, "true", Duration.ofMinutes(30));
        redisTemplate.delete(key);
    }

}
