package com.salayo.locallifebackend.domain.email.service;

import static com.salayo.locallifebackend.global.util.CacheKeyPrefix.EMAIL_CODE;
import static com.salayo.locallifebackend.global.util.CacheKeyPrefix.EMAIL_VERIFIED;

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

        redisTemplate.opsForValue().set(EMAIL_CODE + email, code, Duration.ofSeconds(EXPIRE_TIME));

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
        redisTemplate.delete(EMAIL_VERIFIED + email);
    }

    public void verifyEmailCode(String email, String code) {
        String key = EMAIL_CODE + email;
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
        redisTemplate.opsForValue().set(EMAIL_VERIFIED + email, "true", Duration.ofMinutes(30));
        redisTemplate.delete(key);
    }

    public void sendRejectionMail(String email, String businessName, String rejectReason, String reapplyLink) {
        String subject = "[LocalLife] 로컬 크리에이터 가입 거절 안내 및 재제출 요청";
        String content = "안녕하세요." + businessName + "님\n\n"
            + "아쉽게도 회원님의 로컬 크리에이터 신청이 아래 사유로 인해 거절되었습니다.\n"
            + "거절 사유 : " + rejectReason + "\n\n"
            + "서류 재제출을 원하신다면 아래 링크를 클릭해 안내에 따라 다시 신청해 주세요.\n"
            + reapplyLink + "\n\n"
            + "감사합니다.";

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }

    public void sendPasswordResetCode(String email, String code) {
        String subject = "[LocalLife] 비밀번호 재설정 인증 코드 안내";
        String content = "비밀번호 재설정 인증코드 : " + code + "\n" + "본 코드는 5분간 유효합니다.";

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }

    public void sendPreviewLinkEmail(String email, String businessName, String url) {
        String subject = "[LocalLife] 로컬매거진 초안 확인 링크 및 피드백 요청 안내";

        String text = businessName + "님, 안녕하세요.\n\n"
            + "LocalLife 매거진팀입니다.\n\n"
            + "지난번 인터뷰에 응해주셔서 진심으로 감사드립니다.\n"
            + "인터뷰 내용을 바탕으로 매거진 초안을 작성하여 공유드립니다.\n\n"
            + "아래 링크를 통해 내용을 검토해주시고,\n"
            + "수정하거나 보완할 부분이 있다면 2주 이내에 회신 부탁드립니다.\n\n"
            + "[매거진 초안 확인 링크]\n" + url + "\n\n"
            + "초안 페이지 하단에는 '피드백 남기기' 버튼이 있습니다.\n"
            + "수정 요청이 있으신 경우 해당 버튼을 눌러 내용을 작성해주시면 됩니다.\n\n"
            + "※ 메일 회신은 누락될 수 있으니, 회신은 반드시 '피드백 남기기' 버튼을 통해 남겨주세요.\n\n"
            + "※ 수정 요청은 최대 3회까지 가능하며, \n"
            + "2주 이내 회신이 없을 경우 현재 초안을 기준으로 매거진이 최종 발행될 수 있습니다.\n\n"
            + "확인을 마치고 최종 승인을 주시면, \n"
            + "LocalLife 매거진으로 정식 발행되어 많은 청년들에게 " + businessName + "님의 이야기와 지역이 소개될 예정입니다.\n\n"
            + "감사합니다.\n"
            + "LocalLife 드림";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    public void sendRevisionLinkEmail(String email, String businessName, String url, String subject, String messageHeader, int revisionCount) {

        String text = businessName + "님, 안녕하세요.\n\n"
            + "LocalLife 매거진팀입니다.\n\n"
            + messageHeader + "\n\n"
            + "아래 링크를 통해 글과 사진을 확인해주시고,\n"
            + "추가로 보완할 부분이 있다면 3일 이내에 남겨주시길 부탁드립니다.\n\n"
            + "[수정본 확인 링크]\n" + url + "\n\n"
            + "수정본 페이지 하단의 '피드백 남기기' 버튼을 통해 요청하실 내용을 작성해주시면 됩니다.\n"
            + "메일 회신은 누락될 수 있으니, 꼭 해당 버튼을 통해 남겨주세요.\n\n"
            + "※ 현재까지 반영된 수정 요청: " + revisionCount + "회\n"
            + "※ 수정 요청은 최대 3회까지 가능하며, 이후에는 고객센터를 통해 별도 요청이 가능합니다.\n"
            + "※ 3일 이내 회신이 없을 경우, 현재 수정본을 기준으로 발행이 진행될 수 있습니다.\n\n"
            + "확인을 마치고 최종 승인을 주시면,\n"
            + "LocalLife 매거진으로 정식 발행되어 많은 청년들에게 " + businessName + "님의 이야기와 지역이 소개될 예정입니다.\n\n"
            + "항상 협조해주셔서 감사합니다.\n"
            + "LocalLife 드림";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }
}
