package com.salayo.locallifebackend.domain.member.entity;

import com.salayo.locallifebackend.domain.member.enums.Gender;
import com.salayo.locallifebackend.domain.member.enums.MemberRole;
import com.salayo.locallifebackend.domain.reservation.entity.Reservation;
import com.salayo.locallifebackend.domain.paymenthistory.entity.PaymentHistory;
import com.salayo.locallifebackend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 50)
    private String phoneNumber;

    @Column(nullable = false, length = 50)
    private String birth;

    @Column(nullable = true, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MemberRole memberRole;

    @OneToMany(mappedBy = "member")
    private List<Reservation> reservations = new ArrayList<Reservation>();

    @OneToMany(mappedBy = "member")
    private List<PaymentHistory> paymentHistoryList = new ArrayList<>();

    @Builder
    public Member(String email, String encodedPassword, String phoneNumber, String birth,
        String nickname, Gender gender, MemberRole memberRole) {
        this.email = email;
        this.password = encodedPassword;
        this.phoneNumber = phoneNumber;
        this.birth = birth;
        this.nickname = nickname;
        this.gender = gender;
        this.memberRole = memberRole;
    }
}
