
# ⚙️ LocalLife Backend — Spring Boot API Server

<img width="2839" height="1435" alt="Local_Life_UI_Image_5MB" src="https://github.com/user-attachments/assets/45c4b2f0-7466-4f43-8a59-adc0fffdb1e0" />


<div align="center">
  
---

**LocalLife Backend**는  AI 적성 추천 기반 지역 체험 플랫폼(LocalLife)의 핵심 API 서버로,


인증/인가, 예약/결제, 파일 업로드, 매거진/관리자 운영 등  
서비스 전반의 비즈니스 로직을 담당하는 **Spring Boot 기반 백엔드 서버 애플리케이션**입니다.

![Hackathon](https://img.shields.io/badge/2025%20KDT%20해커톤-예선진출-blue)
![Status](https://img.shields.io/badge/Status-MVP%20Complete-success)
[![기간](https://img.shields.io/badge/프로젝트_기간-2025.07.10~2025.12.20-green?style=flat)]()
[![license](https://img.shields.io/badge/license-Salayo%20Custom%20License-blue)](https://github.com/salayo-team/local-life-backend/blob/main/LICENSE)

</div>

---

## 📑 목차

<a href="#-프로젝트-개요">
  <img src="https://img.shields.io/badge/프로젝트_개요-424242?style=for-the-badge" />
</a>

<a href="#-기술-스택">
  <img src="https://img.shields.io/badge/기술_스택-616161?style=for-the-badge" />
</a>

<a href="#-아키텍처">
  <img src="https://img.shields.io/badge/아키텍처-757575?style=for-the-badge" />
</a>

<a href="#%EF%B8%8F-erd">
  <img src="https://img.shields.io/badge/ERD-9E9E9E?style=for-the-badge" />
</a>

<a href="#-사용자-흐름-user-flow">
  <img src="https://img.shields.io/badge/사용자_흐름-616161?style=for-the-badge" />
</a>

<a href="#-요구사항-명세-srs">
  <img src="https://img.shields.io/badge/요구사항_명세-757575?style=for-the-badge" />
</a>

<a href="#-폴더-구조">
  <img src="https://img.shields.io/badge/폴더_구조-9E9E9E?style=for-the-badge" />
</a>

<a href="#-핵심-기능">
  <img src="https://img.shields.io/badge/핵심_기능-424242?style=for-the-badge" />
</a>

<a href="#-api-문서">
  <img src="https://img.shields.io/badge/API_문서-616161?style=for-the-badge" />
</a>

<a href="#-환경-설정">
  <img src="https://img.shields.io/badge/환경_설정-757575?style=for-the-badge" />
</a>

<a href="#%E2%80%8D-역할-분담">
  <img src="https://img.shields.io/badge/역할_분담-424242?style=for-the-badge" />
</a>


<a href="#-트러블슈팅">
  <img src="https://img.shields.io/badge/트러블슈팅-616161?style=for-the-badge" />
</a>

<a href="#-기여-규칙">
  <img src="https://img.shields.io/badge/기여_규칙-757575?style=for-the-badge" />
</a>

<a href="#-코드-컨벤션">
  <img src="https://img.shields.io/badge/코드_컨벤션-9E9E9E?style=for-the-badge" />
</a>

<a href="#-커밋-컨벤션">
  <img src="https://img.shields.io/badge/커밋_컨벤션-424242?style=for-the-badge" />
</a>

<a href="#-repository-링크">
  <img src="https://img.shields.io/badge/Repository-616161?style=for-the-badge" />
</a>

<a href="#-license">
  <img src="https://img.shields.io/badge/License-757575?style=for-the-badge" />
</a>

---

## 📝 프로젝트 개요

LocalLife Backend는 다음과 같은 기능을 제공합니다.

* **JWT 기반 인증 & 인가**
    * Spring Security, Access/Refresh Token, Redis 기반 토큰 블랙리스트 처리

* **회원 및 로컬 크리에이터 관리**
    * 이메일 인증, 가입/탈퇴, 정보 수정, 크리에이터 신청·승인·증빙자료 관리

* **Spring AI 기반 온보딩 적성 검사 기능**
    * 대화형 문항 생성, AI 분석 JSON 파싱, 세션 TTL·이어하기(Resume)·재검사 제한 관리

* **온보딩 결과(적성·선호 지역)를 기반으로 한 체험 프로그램 추천 기능**
    * RegionType 자동 매핑, QueryDSL 조건 조회 & 랜덤 추천

* **체험 프로그램 등록 / 조회 / 검색 / 삭제**
    * 스케줄 자동 생성, QueryDSL + Full-Text Search + Ngram 기반 검색 및 필터링/정렬

* **예약 생성·승인·취소 및 결제 연동**
    * 예약·환불·검증 통합 설계, PortOne 결제/환불, 결제 이력 관리

* **리뷰 및 필터링·정렬 조회 기능**
    * 별점·좋아요·조회수, 리뷰–프로그램 통계 연동, 복합 필터링 & 정렬 조건 조회

* **이미지 파일 업로드 및 관리 (AWS S3)**
    * FileMapping 기반 용도별 파일 연결 및 삭제·재업로드 처리

* **매거진 및 피드백(관리자–로컬 크리에이터 협업)**
    * 초안·수정 요청·등록 플로우, 피드백 제한, 미리보기 토큰 발급

---

모든 기능은 **RESTful API** 형태로 제공되며, Swagger(Springdoc OpenAPI) 기반 자동 문서화를 사용합니다.

- 📎 [API 명세서 (Swagger)](https://salayo-team.github.io/locallife-api-docs/api-docs.html)

---

## 🛠 기술 스택

| 분류 | 기술 스택 |
|------|-----------|
| **Language** | `Java 21` |
| **Backend Framework** | `Spring Boot 3.3.1`, `Spring Web MVC`, `Spring Data JPA` |
| **AI** | `Spring AI (프롬프트 기반 적성 추천 엔진)`, `OpenAI GPT-4o-mini (ChatGPT API 연동)` |
| **Database & Migration** | `MySQL 8.x`, `Redis`, `Flyway` |
| **Infra / DevOps** | `AWS EC2`, `Docker`, `GitHub Actions` |
| **Auth & Security** | `Spring Security`, `JWT (jjwt)` |
| **Search / Query** | `QueryDSL` |
| **File & External Services** | `AWS SDK S3`, `Spring Cloud AWS`, `PortOne(Iamport)` |
| **Mail & Notification** | `Spring Boot Mail`, `Jakarta Mail API` |
| **Docs & Collaboration** | `Swagger (springdoc-openapi)`, `Notion`, `PDF 문서` |
| **Support Libraries** | `Lombok` |

> Redis는 AccessToken 블랙리스트, RefreshToken 저장, 임시 인증 데이터 캐싱 등에 사용됩니다.

---

## 🏗 아키텍처

### 1) 시스템 아키텍쳐 다이어그램

<img width="1212" height="894" alt="service_archetecture" src="https://github.com/user-attachments/assets/6c47e084-ebba-44e0-9eb3-eda448492b00" />


### 2) 시스템 레벨

```text
Client (Web / App)
        |
        |  REST API
        ↓
Backend (Spring Boot)
 ├── Auth / Security (JWT)
 ├── Member / LocalCreator
 ├── Onboarding / AI Recommendation (Spring AI)
 ├── Program / Category / ProgramSchedule
 ├── Reservation / Payment
 ├── Review
 ├── Magazine / Admin
 ├── File Upload (S3)
        ↓
MySQL / Redis
```

* Spring Boot 기반 백엔드 서버
* DB는 MySQL 단일 인스턴스, 검색은 Full-Text INDEX + Ngram 기반 확장 적용

### 3) DDD + 3-Layer 패턴

- **패키지 구조는 도메인(Domain-based Packaging) 기준으로 나누고**,

  각 도메인 안에서 `Controller → Service → Repository → Entity/DTO`의 3-Layer 구조를 따릅니다.

- 공통 엔티티/에러코드/DTO/설정은 `global` 패키지에서 관리합니다.


### 4) 시퀀스 다이어그램

<img width="1502" height="953" alt="Image" src="https://github.com/user-attachments/assets/99dc9521-2c91-405a-9c35-713c937068bf" />


- 인증, AI 적성 추천, 예약·결제 등 핵심 사용자 흐름에서의 요청 처리 과정을 단계별로 표현

- Spring Security, Spring AI, PortOne이 LocalLife의 인증·추천·결제 흐름에 어떻게 적용되는지를 중심으로 구성

- 각 시퀀스는 실제 도메인 분리 구조 및 코드 흐름과 대응되도록 설계하여,
  기술 선택과 서비스 요구사항 간의 연결 관계가 드러나도록 작성

---

## 🗃️ ERD

ERD는 주요 도메인(회원, 로컬 크리에이터, 프로그램, 예약, 결제, 리뷰, 파일, 매거진, 온보딩, 적성 등)의 관계를 정의합니다.

- 📎 [ERD 전체 보기](https://www.erdcloud.com/d/yiwNwJeESMSkuQD9A)

---

## 🔄 사용자 흐름 (User Flow)

예선 과제 기획서에서 정의한 **청년(일반 사용자) / 로컬 크리에이터 / 관리자**의 사용자 플로우를 기준으로 구현되었습니다.

### 사용자 유형별 주요 플로우 요약

<table>
  <tr>
    <th align="center">유형</th>
    <th align="left">핵심 흐름</th>
  </tr>
  <tr>
    <td align="center"><b>청년(일반 사용자)</b></td>
    <td>
온보딩(적성 진단)  
- 개인화 추천  
- 프로그램 검색/예약/결제  
- 후기 작성  
    </td>
  </tr>
  <tr>
    <td align="center"><b>로컬 크리에이터</b></td>
    <td>
크리에이터 등록  
- 프로그램 등록·관리  
- 예약 승인/거절   
    </td>
  </tr>
  <tr>
    <td align="center"><b>관리자</b></td>
    <td>
로컬 크리에이터 신청·프로그램 검수  
- 승인 상태 관리  
- 매거진/콘텐츠 운영   
    </td>
  </tr>
</table>

---

### 전체 흐름 다이어그램

#### 👤 청년(일반 사용자)

<img width="18636" height="10296" alt="일반 사용자 User Flow" src="https://github.com/user-attachments/assets/7417e9bc-5801-4902-9e26-b22828a41fff" />


#### 🧑‍🌾 로컬 크리에이터

<img width="18636" height="10296" alt="로컬크리에이터 User Flow" src="https://github.com/user-attachments/assets/df869b86-236e-422f-bc5a-507f62ae7950" />


#### 🛠 관리자

<img width="18636" height="10296" alt="관리자 User Flow" src="https://github.com/user-attachments/assets/f8b9a8f5-3433-4984-99bf-8b62b96db86f" />


---

## 📄 요구사항 명세 (SRS)

서비스 기획 및 도메인 설계는 **요구사항 명세서**를 기준으로 진행되었습니다.

- 도메인별 요구사항 (회원, 관리자, 매거진, 체험 프로그램, 예약, 결제, AI 적성 추천, 온보딩, 리뷰, 검색)
- 권한/보안/성능 고려사항 (Soft Delete, 검수 프로세스, 조회수 동시성, N+1 방지 등)

**문서 링크**

- 📎 [요구사항 명세서 (PDF)](https://github.com/salayo-team/local-life-backend/blob/develop/.github/docs/software-requirements-specification.pdf)

---

## 📁 폴더 구조

```
src/
└── main/
    ├── java/
    │   └── com/locallife/backend
    │       ├── LocalLifeBackendApplication.java
    │       │
    │       ├── domain/
    │       │   ├── admin/
    │       │   ├── ai/
    │       │   ├── category/
    │       │   ├── email/
    │       │   ├── file/
    │       │   ├── localcreator/
    │       │   ├── magazine/
    │       │   ├── member/
    │       │   ├── onboarding/
    │       │   ├── payment/
    │       │   ├── program/
    │       │   ├── programschedule/
    │       │   ├── reservation/
    │       │   └── review/
    │       │
    │       └── global/
    │           ├── config/
    │           ├── dto/
    │           ├── entity/
    │           ├── enums/
    │           ├── error/
    │           ├── init/
    │           ├── security/
    │           ├── success/
    │           └── util/
    │
    └── resources/
        ├── db/
        │   └── migration/
        │       └── V1_1__add_fulltext_index.sql
        │
        ├── http/
        │   ├── auth.http
        │   ├── magazine.http
        │   ├── ai-aptitude.http
        │   ├── onboarding.http
        │   ├── program.http
        │   └── reservation.http
        │
        ├── META-INF/
        │   └── services/
        │       └── org.hibernate.boot.model.FunctionContributor
        │
        └── prompts/
            └── aptitude-test.txt

```

### 폴더 설명

- `domain/*`
    - 각 도메인별 하위 패키지로 **Controller, Service, Repository, Entity, DTO**를 분리하여 한 곳에 모아 구성
    - 예시:
        - `domain/reservation/controller` 하위 `ReservationController`, `ReservationTestsController`
        -  `domain/admin/service` 하위 `AdminService`, `AdminAccountService`

- `global/*`
    - 프로젝트 전역에서 사용하는 공통 모듈
    - 예외 처리, 공통 응답 DTO, Enum, Security 설정, 초기 데이터 세팅, 공용 유틸 등
        - Soft Delete 및 State Enum 기반 상태 관리
        - GlobalExceptionHandler + ErrorCode 통합 구조
        - Spring AI 기반 프롬프트 구성 → 응답 파싱 후 추천 로직 반영
        - Redis 활용: JWT Blacklist / RefreshToken / 인증 데이터 캐시

- `resources/*`
    - 애플리케이션 실행 및 기능 동작에 필요한 설정·리소스 파일 관리
    - 환경 설정, DB 마이그레이션, 테스트 요청 파일, AI 프롬프트 등을 포함

    - `db/migration/`
        - Flyway 기반 DB 스키마 및 인덱스 마이그레이션 SQL 관리
        - 검색을 위한 Full-Text INDEX / Ngram Parser 설정 포함

    - `http/`
        - 도메인별 API 테스트를 위한 HTTP 요청 파일 모음
        - 인증, 매거진, 온보딩, AI 적성 검사, 프로그램, 예약 등 주요 플로우 검증 용도

    - `META-INF/services/`
        - Hibernate SPI(Service Provider) 경로 등록
        - MySQL 커스텀 함수(FunctionContributor) Hibernate/JPA 연동

    - `prompts/`
        - Spring AI에서 사용하는 프롬프트 템플릿 관리
        - 적성 검사 질문 흐름 및 응답 형식 정의

---

## 🔑 핵심 기능

### 1) 인증/인가(Auth)
- Spring Security 기반 인증 구조 설계
- JWT AccessToken & RefreshToken 발급/검증
- Redis 기반 AccessToken Blacklist 처리
- 로그인 / 로그아웃 / 토큰 재발급
- 사용자 권한(Role: USER·CREATOR·ADMIN) 기반 접근 제어

### 2) 회원(Member)
- 이메일 인증(코드 발송·검증)
- 회원 가입 로직(닉네임 생성, 중복 검증 등)
- 로그인 시 이메일 인증 및 탈퇴 상태 체크
- 비밀번호 재설정(코드 발송·검증 후 업데이트)
- 회원 정보 수정(닉네임·전화번호), 탈퇴 처리

### 3) 로컬 크리에이터(LocalCreator)
- 로컬 크리에이터 등록 신청
- 증빙자료 업로드(S3) + FileMapping 기반 연결 관리
- 운영자 승인/거절/삭제 처리
- 거절 시 재신청용 토큰 발급(유효기간 포함)
- 승인된 후 재수정 시 재승인 상태로 변경되는 로직

### 4) AI 적성 추천(AIAptitude)
- 사용자 답변을 기반으로 적성 유형을 분석하는 Spring AI 기반 적성 검사
- 질문/응답을 단계별로 저장하여 이어갈 수 있는 세션 기반 검사 진행 관리
- 검사 종료 후 사용자 맞춤형 적성 유형 및 선호 지역 반환
- 온보딩 완료 후 마이페이지에서 AI 적성 재검사

### 5) 온보딩(Onboarding)
- 온보딩 단계 진행 상황 추적 및 이어하기
    - 지역 선택 → 적성 검사 → 체험 프로그램 추천
- 선택한 지역 유형을 실제 지역 목록으로 연결하는 선호 지역 매핑
- 온보딩 중 잘못된 요청을 방지하는 단계 유효성 검증
- 적성 정보·선호 지역을 기반으로 한 맞춤 체험 프로그램 추천

### 6) 체험 프로그램(Program)
- 체험 프로그램 생성
    - 시작/종료일 + 요일 + 회차 정보 등 조합해 전체 스케줄 자동 생성
- 체험 프로그램 삭제
    - 예약 상태 & 시작일에 따라 삭제 가능 여부 검증
- 체험 프로그램 전체 리스트 조회, 상세 조회(이미지 포함)
- 체험 프로그램 검색
    - QueryDSL + Full-Text Search + Ngram 기반 기능 구현
    -  제목·사업자명, 지역·적성 카테고리, 가격 범위, 정렬
- Flyway 적용하여 DB INDEX 설정 관리

### 7) 예약(Reservation)
- 예약 신청(예약 검증 및 중복 예약 방지 처리)
- 로컬 크리에이터 예약 거절, 멤버 예약 취소
- 예약 상태 변경(거절·취소)에 따른 결제/환불 연동 처리

### 8) 결제(Payment) & 결제 이력(PaymentHistory)
- PortOne API 사용하여 결제 기능 구현
- 결제 사전 생성
    - 결제 진행상 오류 발생시 대응 가능하도록 설계
- 결제 검증
    - PortOne 응답과 LocalLife DB 상태를 비교하여 검증
    - 중복 결제 방지 & 검증 실패시 실패 내역 결제 이력 저장
- 결제 이력 저장
    - 결제 검증, 환불, 성공 및 실패 상황까지
      발생한 이벤트를 전부 이력으로 기록
- 결제 환불
    - 전액 환불, 부분 환불, 다중 환불 진행 가능
- 관리자 전용 환불
    - 예약, 결제 상태에 상관없이 운영상 필요에 따라 환불

### 9) 후기(Review)
- 사용자 후기 작성 / 조회 / 수정 / 삭제
- 별점·좋아요·조회수를 기반으로 한 참여도 집계
- 로컬 크리에이터 후기 답글 작성(답글 작성 후 사용자 후기 수정 불가)
- QueryDSL 기반 태그·키워드·카테고리 조건을 조합한 후기 필터링 조회
- 후기 생성/수정/삭제 시 프로그램 도메인의 리뷰 집계 필드의 데이터를 갱신

### 10) 파일 업로드(File)
- S3 기반 이미지·증빙자료 업로드
- FileCategory·FilePurpose 기반 파일 용도 관리
- FileMapping 엔티티로 사용자/매거진 등과 파일 연결
- 기존 파일 삭제 후 새 파일 업로드 처리

### 11) 관리자(Admin)
- 로컬 크리에이터 가입 승인/거절
- 거절 시 재신청 토큰 발급
- 이메일 기반 로컬 크리에이터 단건 조회
- 관리자 전용 계정 생성 로직

### 12) 매거진(Magazine)
- 매거진 생성 / 임시저장 / 수정 / 삭제
- 관리자 ↔ 로컬 크리에이터 협업 플로우 설계
    - 수정 제안 요청 · 확인 · 등록 상태 관리
    - 피드백 작성 / 조회 / 삭제
    - 피드백 최대 3회 제한, 미반영 피드백 존재 시 추가 제출 금지
- 미리보기 토큰 발급(유효기간 포함) 및 접근 검증
- HTML 이미지 URL 파싱 후 FileMapping과 매거진 ID 매핑

### 13) 공통(Global)
- 전역 예외 처리(GlobalExceptionHandler)
- ErrorCode·SuccessCode 설계
- CommonResponseDto·CommonListResponseDto 설계
- 프로젝트 초기 구조 설정(Security·Swagger·SoftDelete 등)

---

## 📚 API 문서

Swagger(Springdoc OpenAPI) 기반의 자동 문서화를 사용합니다.

- 로컬 실행 시:
    - `http://localhost:8080/swagger-ui/index.html`
- GitHub Pages:
    - `https://salayo-team.github.io/locallife-api-docs/api-docs.html`

Swagger 관리 원칙:

- `@Tag`, `@Operation`, `@ApiResponse`, `@Parameter`를 활용해
  **요약/설명/예시/제약조건**을 명확히 문서화합니다.

- 📎 [Swagger 문서화 관리 상세 설명 보기](https://github.com/salayo-team/local-life-backend/blob/develop/.github/docs/swagger_documentation.pdf)


---

## 🔧 환경 설정

LocalLife Backend는 DB, Redis, JWT, AWS S3, AI(OpenAI), 결제(PortOne) 연동을 포함한
다수의 외부 의존성을 환경 변수 기반으로 설정합니다.

* 환경 변수는 `application.properties` 또는 OS 환경 변수로 주입
* 보안 정보(API Key, Secret 등)는 **레포지토리에 커밋하지 않고 `.env`로 관리**
* JPA(Hibernate)를 중심으로 스키마를 관리하며,
  검색 성능 최적화를 위한 인덱스 설정만 Flyway로 부분 적용

프로젝트 실행을 위해 필요한 **전체 환경 변수 목록, 설정 예시,
JPA & Flyway 사용 방식, 로컬 실행 시 주의 사항**은 아래 문서에서 상세히 확인할 수 있습니다.

📎 **[Local Life Project 환경 설정 및 실행 방법 상세 설명 보기](https://github.com/salayo-team/local-life-backend/blob/develop/.github/docs/execution_instructions.pdf)**

---

## 🧑🏻‍💻 역할 분담

⭐ **김지윤 (Backend)**

* Spring Security · JWT · Redis 기반 인증·인가 아키텍처 구축
* 사용자 가입/인증/권한 처리 (USER · CREATOR · ADMIN)
* 파일 업로드(S3), 매거진·피드백(관리자-로컬크리에이터 협업) 기능 개발
* 전역 예외·응답 구조 설계 및 초기 세팅

⭐ **한지연 (Backend)**

* 체험 프로그램 도메인 전반 설계 및 운영 로직 구현
    * QueryDSL + Full-Text Search + Ngram 기반 검색·정렬 구조 설계
    * Flyway 기반 DB 인덱스 및 스키마 변경 관리
* 예약 상태 흐름(신청·거절·취소) 및 검증 로직 설계
* PortOne API 연동 결제·환불·검증 및 결제 이력 관리 구조 구축

⭐ **허수연 (Backend)**

* AI 기반 온보딩 및 개인화 추천 시스템 전체 설계 (Spring AI)
    * 지역 매핑/AI 적성 검사/체험 추천 기능 및 SpringAI 파이프라인 구축
* 리뷰 도메인 복합 구조 설계 및 필터링 정렬 도메인 로직 구현
* QueryDSL 동적 검색·정렬, Redis 기반 캐시·조회 성능 최적화
* 공통 프롬프트/컨텍스트 관리 구조 설계, 문서화 총괄

---

## 🔥 트러블슈팅

아래 표는 팀원별 **트러블슈팅 문서(블로그·노션 등)** 를 정리하기 위한 공간입니다.


<div align="center">

<table>
  <tr>
    <td align="center" width="250">
      <img src="https://github.com/jiyoon0000.png" width="80" style="border-radius: 50%; border: 2px solid #ddd;" /><br/>
      <b>김지윤</b><br/>
      <sub>인증/인가 · 파일/S3 업로드 · 매거진/피드백</sub><br/><br/>
      <a href="https://jy3574.tistory.com/entry/locallife" target="_blank" rel="noopener noreferrer">
  🔗 Troubleshooting 링크
</a>
    </td>
    <td align="center" width="250">
      <img src="https://github.com/j-hann.png" width="80" style="border-radius: 50%; border: 2px solid #ddd;" /><br/>
      <b>한지연</b><br/>
      <sub>체험 프로그램 · 예약 · 결제 · 검색/필터링 정렬</sub><br/><br/>
      <a href="https://uhnn-archive.tistory.com/578" target="_blank" rel="noopener noreferrer">
  🔗 Troubleshooting 링크
</a>
    </td>
    <td align="center" width="250">
      <img src="https://github.com/sooyeoneo.png" width="80" style="border-radius: 50%; border: 2px solid #ddd;" /><br/>
      <b>허수연</b><br/>
      <sub>AI 적성 추천 · 온보딩 · 리뷰 · 필터링 정렬</sub><br/><br/>
      <a href="https://sooyeoneo.tistory.com/162" target="_blank" rel="noopener noreferrer">
  🔗 Troubleshooting 링크
</a>
    </td>
  </tr>
</table>

</div>

---

## 🤝 기여 규칙

### 📌 자세한 기준은 아래 이슈를 반드시 참고해주세요

👉 [**Contributing Detail Issue 기여 문서 상세 내용 확인하기**](https://github.com/salayo-team/local-life-backend/issues/182)

### Branch 전략

| 브랜치 | 설명 |
| --- | --- |
| `main` | 배포용 안정 버전 |
| `develop` | 통합 개발 브랜치 |
| `feature/*` | 기능/도메인 단위 작업 브랜치 (예: `feature/review`) |
| `hotfix/*` | 운영 중 긴급 수정 |

### PR 규칙

1. 이슈 번호와 함께 작업 내용 명확히 작성
2. 기능/버그/리팩토링 등 라벨 지정
3. 스스로 로컬 테스트 후 PR 생성
4. 모든 리뷰어 승인 후 Squash & Merge

---

## 🧾 코드 컨벤션

#### Code Convention 문서를 기반으로 합니다.

- 📎 [Code Convention 문서](https://github.com/salayo-team/local-life-backend/blob/develop/.github/docs/code_convention.pdf)

주요 원칙:

- **Setter/Getter 지양**, 의미 있는 메서드로 상태 변경
- **Enum 기반 상태/에러코드 관리**
- **GlobalExceptionHandler + ErrorCode Enum**으로 예외 처리 통합
- **Import 와일드카드 금지 (`import java.util.*;` X)**
- `Impl` 네이밍의 Service 클래스 금지 (인터페이스/구현 분리 대신 명확한 서비스 명 사용)
- **메서드 네이밍 규칙**
    - `createXxx`, `getXxx`, `updateXxx`, `deleteXxx` 형태로 HTTP 메서드와 매핑
- **주석/로깅**
    - 복잡한 로직에는 반드시 주석
    - `@Slf4j` 기반 INFO/DEBUG/ERROR 로깅 규칙 적용
- **검증**
    - DTO 레벨에서 Bean Validation으로 입력값 검증
- **성능**
    - N+1 방지를 위한 QueryDSL, fetch join, 필요한 필드만 조회, 페이징 적용

---

## 🧩 커밋 컨벤션

### GitHub Rules 문서에서 정의한 **gitmoji 기반 커밋 컨벤션**을 사용합니다.

📎 [github rules 문서](https://github.com/salayo-team/local-life-backend/blob/develop/.github/docs/github_rules.pdf)


#### 📝 커밋 타입

| 이모지 | 타입 | 설명 |
|--------|------|------|
| 🎉 | `begin` | 프로젝트 시작 |
| ✨ | `feat` | 새로운 기능 추가, 구현 |
| 📝 | `docs` | 문서 파일 추가 및 수정 |
| 🔧 | `add` | 기존 기능에 코드 추가 & 코드 수정 |
| ✏️ | `typos` | 단순 오타 수정 |
| 🐛 | `fix` | 버그 및 에러 수정 |
| ✅ | `test` | 테스트 코드 작성 & 수정 |
| 🚚 | `rename` | 파일, 경로를 옮기거나 이름 변경 |
| ♻️ | `refactor` | 코드 리팩토링 |
| 💡 | `comment` | 주석 추가, 변경 |
| 🔥 | `remove` | 파일, 코드 삭제 |
| 🔀 | `branch` | 브랜치 추가, 병합 등 |
| ️🏗️ | `chore` | 빌드 업무 수정, 패키지 매니저 수정, 패키지 관리자 구성 등 |


**예시**

```bash
✨ feat: 회원 가입 API 구현
🐛 fix: 예약 승인 시 중복 체크 로직 수정
📝 docs: Swagger 리뷰 도메인 문서화
♻️ refactor: ProgramService 쿼리 최적화

```

---

## 📬 Repository 링크

- **Organization**: https://github.com/salayo-team
- **Backend**: https://github.com/salayo-team/local-life-backend

---

## 📄 License

본 프로젝트는 **2025 K-Digital Training 해커톤 및 포트폴리오 목적**으로 개발되었습니다.

- ✅ 교육 및 참고 목적의 사용 가능
- ❌ 사전 동의 없는 상업적 이용 금지
- ⚠️ 코드 사용 시 **출처(LocalLife / Salayo Team) 명시 필수** 및 팀원에게 사전 문의 필요

---

<div align="center">

**LocalLife** - 지역과 청년이 함께 성장하는 플랫폼  
Made by **Salayo Team**

</div>