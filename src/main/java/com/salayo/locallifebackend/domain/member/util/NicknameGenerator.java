package com.salayo.locallifebackend.domain.member.util;

import java.util.List;
import java.util.Random;

public class NicknameGenerator {

    private static final List<String> ADJECTIVES = List.of(
        "활발한", "민첩한", "따뜻한", "섬세한", "유쾌한", "창의적인", "유연한",
        "똑똑한", "씩씩한", "자유로운", "꼼꼼한", "성실한", "꾸준한", "정직한", "신중한"
    );

    private static final List<String> NOUNS = List.of(
        "나무", "달빛", "숲길", "별빛", "구름", "고래", "나비",
        "바람", "생각", "시선", "솜씨", "물결", "파도", "리듬", "눈빛"
    );

    private static final Random random = new Random();

    public static String generate() {
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String noun = NOUNS.get(random.nextInt(NOUNS.size()));
        int number = random.nextInt(900) + 1;

        String formattedNumber = String.format("%03d", number);

        return adjective + noun + formattedNumber;
    }
}
