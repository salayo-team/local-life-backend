package com.salayo.locallifebackend.domain.admin.util;

import com.salayo.locallifebackend.domain.admin.service.AdminAccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class AdminAccountCommandLineRunner implements CommandLineRunner {

    private final AdminAccountService adminAccountService;

    public AdminAccountCommandLineRunner(AdminAccountService adminAccountService) {
        this.adminAccountService = adminAccountService;
    }

    @Override
    public void run(String... args) {

        String adminsEnv = System.getenv("ADMIN_LIST");

        if (adminsEnv == null || adminsEnv.isBlank()) {
            System.out.println("ADMIN_LIST가 설정되어 있지 않습니다.(계정 생성 X)");
            return;
        }

        String[] admins = adminsEnv.split(",");

        for (String adminInfo : admins) {
            String[] fields = adminInfo.trim().split(":");

            if (fields.length < 3) {
                System.out.println("잘못된 형식의 관리자 정보 : " + adminInfo + "ex. email:password:nickname");
                continue;
            }

            String email = fields[0].trim();
            String pw = fields[1].trim();
            String nickname = fields[2].trim();

            try {
                adminAccountService.createAdmin(email, pw, nickname);
                System.out.println("관리자 계정 생성 : " + email + "(" + nickname + ")");
            } catch (Exception e) {
                System.out.println(email + "계정 생성 실패 : " + e.getMessage());
            }
        }
    }

}
