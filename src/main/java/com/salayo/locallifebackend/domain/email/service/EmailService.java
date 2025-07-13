package com.salayo.locallifebackend.domain.email.service;

import java.time.Duration;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Qualifier("emailVerifiedRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

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

}
