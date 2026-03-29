# Simple CMS REST API

2026년도 신입 Back-End 개발자 채용 과제 제출물입니다.
<br>
<br>
단순한 기능 구현을 넘어 향후 서비스 확장을 고려하여 관심사가 명확히 분리된 레이어드 아키텍처를 설계하였으며, 일관된 규격의 커스텀 예외 처리 구조를 구축해 유지보수성을 높였습니다.

---

## 1. 프로젝트 개요

### Spec

| 분류 | 기술 |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.3 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| ORM | Spring Data JPA + QueryDSL 7.1 |
| Database | H2 (In-Memory) |
| Docs | Springdoc OpenAPI 3.0.0 |
| Build | Gradle |
| Etc | Lombok, p6spy |

---

### 배포 현황

| 구분 | 주소 |
|---|---|
| **백엔드 API** | https://kcjin-malgn-cms.o-r.kr |
| **프론트엔드** | https://kcjin-malgn-cms.vercel.app |
| **Swagger UI** | https://kcjin-malgn-cms.o-r.kr/swagger-ui/index.html |

> 백엔드: Naver Cloud Platform (mi1-g3 · vCPU 1 · Memory 1GB · Ubuntu 24.04)
>
> 프론트엔드: Vercel.

Spring Boot 구동에 다소 낮은 사양이나, 트래픽이 없는 환경임을 감안하여 가장 경제적인 사양의 인스턴스를 선택했습니다.

---

## 2. 실행 및 테스트 가이드

### 로컬 실행 방법

#### 환경 변수

프로젝트 보안과 편의성을 위해 설정값은 `.env` 파일로 분리하여 관리합니다.<br>
또한 모든 환경 변수에 대해 `application.yml` 파일 내 기본값을 설정해 두어 별도의 환경 구성 없이도 즉시 실행이 가능합니다.

>원칙적으로 환경변수 파일은 `.gitignore`에 포함되어야 하나, 과제 검토의 편의성을 위해 예외적으로 포함하였습니다.<br>

| 변수명 | 기본값 | 설명 |
|---|---|---|
| `JWT_SECRET` | (기본값 설정됨) | JWT 서명 키 |
| `ACCESS_TOKEN_EXPIRATION` | `3600000` | Access Token 만료 시간 (ms, 1시간) |
| `REFRESH_TOKEN_EXPIRATION` | `1209600000` | Refresh Token 만료 시간 (ms, 14일) |
| `DB_USERNAME` | `sa` | H2 DB 사용자명 |
| `DB_PASSWORD` | (없음) | H2 DB 비밀번호 |

#### 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

서버 기본 포트는 **8080**입니다.

---

#### 로컬 접속 정보

| 항목 | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| H2 Console | http://localhost:8080/h2-console |

**H2 Console 접속 정보**

- JDBC URL: `jdbc:h2:mem:test`
- Username: `sa`
- Password: (없음)

---

### 초기 계정

애플리케이션 시작 시 아래 계정이 자동 생성됩니다. <br>
(비밀번호:`1234`)

| username | role | 비고 |
|---|---|---|
| `admin` | ADMIN | 모든 콘텐츠 수정·삭제 가능 |
| `user1` | MEMBER | 본인 콘텐츠만 수정·삭제 가능 |
| `user2` | MEMBER | 본인 콘텐츠만 수정·삭제 가능 |

---

## 3. 시스템 구조 및 구현 기능

### 패키지 구조
소규모 프로젝트이지만 추후 확장성을 고려해 레이어드 아키텍처로 관심사를 명확히 분리했습니다.

```
src/main/java/com/malgn
├── presentation          # 요청/응답 처리
│   ├── api               # Swagger 명세 인터페이스 (Controller 코드 가독성을 위해 분리)
│   ├── controller
│   ├── advice            # GlobalExceptionHandler
│   └── dto               # Request / Response DTO
├── application           # 비즈니스 로직
│   ├── service           # ContentService, AuthService, AdminService
│   └── dto               # TokenDto (레이어 간 전달 객체)
├── domain                # 핵심 도메인
│   ├── model             # Entity, ErrorCode, MemberRole
│   ├── exception         # BusinessException 계층 (Auth / Content / Member)
│   └── repository        # Repository 인터페이스
└── infrastructure        # 외부 관심사
    ├── security          # JWT 필터, EntryPoint, UserDetails
    ├── persistence       # JPA Auditing
    ├── repository        # QueryDSL 구현체 (ContentRepositoryImpl)
    └── configure         # Bean 설정 (Swagger, QueryDSL, CORS)
```

---

### 테이블 명세서

#### 회원

컬럼명|데이터 타입|제약 조건|설명
--|--|--|--
id|BIGINT|PK, AUTO_INCREMENT|회원 식별자
username|VARCHAR(50)|NOT NULL, UNIQUE|로그인 아이디
password|VARCHAR(255)|NOT NULL|BCrypt 암호화 저장
name|VARCHAR(50)|NOT NULL|회원 이름
role|VARCHAR(255)|NOT NULL|권한 (MEMBER, ADMIN)
created_date|DATETIME|NOT NULL|가입 일시
created_by|VARCHAR(50)|NOT NULL|생성자

#### 콘텐츠
컬럼명|데이터 타입|제약 조건|설명
--|--|--|--
id|BIGINT|PK, AUTO_INCREMENT|콘텐츠 식별자
title|VARCHAR(100)|NOT NULL|게시글 제목
description|TEXT|NULL|게시글 내용
view_count|BIGINT|NOT NULL|조회수 (기본값: 0)
member_id|BIGINT|FK, NOT NULL|작성자 외래키 (members.id)
created_date|DATETIME|NOT NULL|작성 일시
created_by|VARCHAR(50)|NOT NULL|생성자

### 구현 기능

#### 1. 인증

- 회원가입 `POST /api/v1/auth/sign-up`
- 로그인 `POST /api/v1/auth/sign-in` — Access Token(바디) + Refresh Token(HttpOnly 쿠키)
- 토큰 재발급 `POST /api/v1/auth/reissue`

#### 2. 콘텐츠 관리

- 목록 조회 `GET /api/v1/contents` — 페이징, 최신순 정렬
- 검색 `GET /api/v1/contents/search` — 제목(contains), 작성자(equals) 동적 검색 (QueryDSL)
- 상세 조회 `GET /api/v1/contents/{id}` — 조회 시 viewCount 자동 증가
- 생성 `POST /api/v1/contents` — 인증 필요
- 수정 `PATCH /api/v1/contents/{id}` — 본인 또는 ADMIN만 가능
- 삭제 `DELETE /api/v1/contents/{id}` — 본인 또는 ADMIN만 가능

#### 3. 관리자 기능

- 권한 변경 `PATCH /api/v1/admin/{id}/role` — ADMIN 전용

API에 대한 자세한 내용은 [API_DOCS](https://github.com/qqqqq7666/malgn-cms/blob/main/2026-cms-api/API_DOCS.md)에 있습니다.

---

## 4. 핵심 설계 및 기술적 의사 결정

### 1. 비관적 락 도입을 통한 동시성 제어
게시글 상세 조회 시 증가하는 `view_count` 필드는 다수의 사용자가 동시에 접근할 경우 `Lost update`가 발생할 위험이 크다고 판단했습니다.
이를 방지하기 위해 비관적 락을 도입해 데이터의 정합성을 높였습니다.

### 2. JPA Entity와 도메인 모델 통합

완벽한 DDD라면 순수 JPA Entity와 도메인 모델을 분리해야 합니다. 하지만 본 과제에서는 다음과 같은 이유로 분리하지 않았습니다.
- 오버엔지니어링 방지: 현재 비즈니스 규모 대비 객체 간 컨버팅 코드가 지나치게 많아져 유지보수 비용이 증가합니다.
- JPA 이점 활용: Entity를 도메인 모델로 변환하여 다루게 되면 JPA가 제공하는 Lazy Loading, Dirty Checking 등의 강력한 기능을 활용하기 어렵습니다.

### 3. 인덱스 전략

데이터베이스 테이블 설계 시 무분별한 인덱스 생성을 지양하고, 실제 쿼리의 `WHERE` 절과 `ORDER BY` 절에 빈번하게 사용되는 `id`와 `created_date` 컬럼에만 인덱스를 적용했습니다.

### 4. JWT 인증 방식 — AuthenticationManager 활용

`AuthenticationManager`를 통해 Spring Security의 표준 인증 흐름을 준수했습니다. 아래와 같이 토큰을 분리하여 전송함으로써 XSS 공격 대응을 강화하고 보안 수준을 높였습니다.

- Access Token -> ResponseBody
- Refresh Token -> HttpOnly + Secure Cookie

### 5. JWT 필터 예외 처리 — JwtAuthenticationEntryPoint

Spring의 `@ControllerAdvice`는 DispatcherServlet 이전에 실행되는 필터 계층의 예외를 처리할 수 없기 때문에, Security Filter Chain 내부에서 발생하는 JWT 만료 및 위변조 예외를 잡지 못합니다.<br> 이를 해결하기 위해 `JwtAuthenticationEntryPoint`를 구현하여 필터에서 발생한 인증 오류도 `ApiResponse` 포맷의 JSON 응답으로 일관되게 반환하도록 처리했습니다.

### 6. Refresh Token 재발급 — `@CookieValue(required = false)`

`required = true`로 두면 Spring이 자동으로 `MissingRequestCookieException`을 던져 커스텀 에러 코드 형식을 통일하기 어렵습니다. `required = false`로 두고 내부에서 직접 `AuthException(ErrorCode.TOKEN_NOT_FOUND)`를 던지는 방식을 택해 에러 응답 형식과 로그 추적을 일관되게 유지했습니다.

### 7. 전역 예외 처리
시스템 확장을 대비해 `ErrorCode` enum과 최상위 예외 `BusinessException`을 기반으로 도메인별 계층형 커스텀 예외 구조를 설계했습니다.<br>
`@ControllerAdvice`를 통해 비즈니스 예외를 하나의 타입으로 일괄처리하며 향후 도메인이 추가되어도 예외 핸들러 로직의 수정이 불필요한 OCP 원칙을 준수했습니다.

---

## 5. 활용한 AI 도구

| 구분 | 활용 내용                                                             |
|---|-------------------------------------------------------------------|
| **Claude** | - 코드 리뷰<br> - QueryDSL 동적 쿼리 구현<br> - 테스트 유즈케이스 도출<br>- 문서 작성     |
| **Gemini** | - 프론트엔드 페이지 구현 전반<br> - Nginx 설정<br> - Vercel 배포 설정<br>- 아키텍처 구조  |
