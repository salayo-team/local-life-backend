package com.salayo.locallifebackend.domain.member.service;

import com.salayo.locallifebackend.domain.file.entity.File;
import com.salayo.locallifebackend.domain.file.entity.FileMapping;
import com.salayo.locallifebackend.domain.file.enums.FileCategory;
import com.salayo.locallifebackend.domain.file.enums.FilePurpose;
import com.salayo.locallifebackend.domain.file.repository.FileMappingRepository;
import com.salayo.locallifebackend.domain.file.repository.FileRepository;
import com.salayo.locallifebackend.domain.file.util.S3Uploader;
import com.salayo.locallifebackend.domain.localcreator.dto.LocalCreatorSignupRequestDto;
import com.salayo.locallifebackend.domain.localcreator.dto.LocalCreatorSignupResponseDto;
import com.salayo.locallifebackend.domain.localcreator.entity.LocalCreator;
import com.salayo.locallifebackend.domain.localcreator.repository.LocalCreatorRepository;
import com.salayo.locallifebackend.domain.member.dto.LoginRequestDto;
import com.salayo.locallifebackend.domain.member.dto.LoginResponseDto;
import com.salayo.locallifebackend.domain.member.dto.UserSignupRequestDto;
import com.salayo.locallifebackend.domain.member.dto.UserSignupResponseDto;
import com.salayo.locallifebackend.domain.member.entity.Member;
import com.salayo.locallifebackend.domain.member.enums.Gender;
import com.salayo.locallifebackend.domain.member.enums.MemberRole;
import com.salayo.locallifebackend.domain.member.repository.MemberRepository;
import com.salayo.locallifebackend.domain.member.util.NicknameGenerator;
import com.salayo.locallifebackend.global.error.ErrorCode;
import com.salayo.locallifebackend.global.error.exception.CustomException;
import com.salayo.locallifebackend.global.security.jwt.JwtProvider;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocalCreatorRepository localCreatorRepository;
    private final S3Uploader s3Uploader;
    private final FileRepository fileRepository;
    private final FileMappingRepository fileMappingRepository;
    private final JwtProvider jwtProvider;

    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
        LocalCreatorRepository localCreatorRepository, S3Uploader s3Uploader,
        FileRepository fileRepository, FileMappingRepository fileMappingRepository,
        JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.localCreatorRepository = localCreatorRepository;
        this.s3Uploader = s3Uploader;
        this.fileRepository = fileRepository;
        this.fileMappingRepository = fileMappingRepository;
        this.jwtProvider = jwtProvider;
    }

    public UserSignupResponseDto signupUser(UserSignupRequestDto requestDto) {

        if (memberRepository.existsByEmail(requestDto.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        String nickname = requestDto.getNickname();
        if (nickname == null || nickname.isBlank()) {

            do {
                nickname = NicknameGenerator.generate();
            } while (memberRepository.existsByNickname(nickname));
        } else if (memberRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        Member member = Member.builder()
            .email(requestDto.getEmail())
            .encodedPassword(passwordEncoder.encode(requestDto.getPassword()))
            .birth(requestDto.getBirth())
            .nickname(nickname)
            .gender(Gender.FEMALE)
            .memberRole(MemberRole.USER)
            .build();

        memberRepository.save(member);

        return new UserSignupResponseDto(member.getNickname(), "일반회원");
    }

    public LocalCreatorSignupResponseDto signupLocalCreator(
        LocalCreatorSignupRequestDto requestDto, List<MultipartFile> files,
        List<FilePurpose> filePurposes) {

        if (memberRepository.existsByEmail(requestDto.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.builder()
            .email(requestDto.getEmail())
            .encodedPassword(passwordEncoder.encode(requestDto.getPassword()))
            .birth(requestDto.getBirth())
            .phoneNumber(requestDto.getPhoneNumber())
            .nickname(null)
            .gender(Gender.FEMALE)
            .memberRole(MemberRole.LOCAL_CREATOR)
            .build();

        memberRepository.save(member);

        LocalCreator localCreator = LocalCreator.builder()
            .member(member)
            .businessName(requestDto.getBusinessName())
            .businessAddress(requestDto.getBusinessAddress())
            .build();

        localCreatorRepository.save(localCreator);

        if (filePurposes == null || files == null || files.size() != filePurposes.size()) {
            throw new CustomException(ErrorCode.INVALID_FILE_PURPOSE_MAPPING);
        }

        for (int i = 0; i < filePurposes.size(); i++) {
            FilePurpose purpose = filePurposes.get(i);

            MultipartFile file = (i < files.size()) ? files.get(i) : null;

            if (purpose == FilePurpose.BANK_ACCOUNT_COPY && (file == null || file.isEmpty())) {
                throw new CustomException(ErrorCode.MISSING_REQUIRED_FILE);
            }

            if (file == null || file.isEmpty()) {
                continue;
            }

            String storeUrl = s3Uploader.upload(file, "local-creator");

            File savedFile = fileRepository.save(
                File.builder()
                    .originalName(file.getOriginalFilename())
                    .storedFileName(storeUrl)
                    .build()
            );

            fileMappingRepository.save(FileMapping.builder()
                .file(savedFile)
                .fileCategory(FileCategory.LOCAL_CREATOR)
                .referenceId(member.getId())
                .filePurpose(purpose)
                .build());
        }

        return new LocalCreatorSignupResponseDto(localCreator.getBusinessName());
    }

    public LoginResponseDto login(LoginRequestDto requestDto) {
        Member member = memberRepository.findByEmail(requestDto.getEmail())
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGIN));

        if (!passwordEncoder.matches(requestDto.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_LOGIN);
        }

        if (member.getMemberRole() == MemberRole.LOCAL_CREATOR) {
            LocalCreator creator = localCreatorRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_LOGIN));
            if (!creator.isApproved()) {
                throw new CustomException(ErrorCode.CREATOR_NOT_APPROVED);
            }
        }

        String accessToken = jwtProvider.generateAccessToken(member.getEmail(), member.getMemberRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(member.getEmail(), member.getMemberRole().name());

        return LoginResponseDto.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .build();
    }
}
