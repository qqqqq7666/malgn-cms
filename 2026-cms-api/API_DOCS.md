# REST API Docs

Base URL (로컬): `http://localhost:8080`  
Base URL (배포): `https://kcjin-malgn-cms.o-r.kr`<br>
Swagger UI: `https://kcjin-malgn-cms.o-r.kr/swagger-ui/index.html`

인증이 필요한 API는 요청 헤더에 아래를 포함해야 합니다.

```
Authorization: Bearer {accessToken}
```

---

## 공통 응답 포맷

모든 API는 아래 형식으로 응답합니다.

**성공**

```json
{
  "success": true,
  "data": { }
}
```

**실패**

```json
{
  "success": false,
  "error": {
    "status": 401,
    "errorCode": "A002",
    "message": "유효하지 않은 토큰입니다."
  }
}
```

---

## 에러 코드표

| 코드 | HTTP | 설명 |
|---|---|---|
| C001 | 404 | 콘텐츠가 존재하지 않습니다 |
| M001 | 404 | 존재하지 않는 사용자입니다 |
| M002 | 409 | 이미 존재하는 회원 아이디입니다 |
| A001 | 403 | 해당 요청에 대한 권한이 없습니다 |
| A002 | 401 | 유효하지 않은 토큰입니다 |
| A003 | 401 | 만료된 토큰입니다 |
| A004 | 401 | 지원되지 않는 토큰 형식입니다 |
| A005 | 401 | 토큰 서명이 올바르지 않습니다 |
| A006 | 401 | 토큰이 존재하지 않습니다 |
| A007 | 401 | 아이디 또는 비밀번호가 일치하지 않습니다 |
| S001 | 500 | 서버 내부 오류가 발생했습니다 |
| S002 | 400 | 잘못된 입력값입니다 |
| S003 | 404 | 요청에 대한 데이터가 존재하지 않습니다 |

---

## 01. 인증 API

### 회원가입

```
POST /api/v1/auth/sign-up
```

**Request Body**

```json
{
  "username": "testuser",
  "password": "passw0rd",
  "name": "홍길동"
}
```

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| username | String | Y | 최대 50자, 중복 불가 |
| password | String | Y | |
| name | String | Y | 최대 50자 |

**Response** `201 Created`

```json
{
  "success": true,
  "data": {
    "id": 4,
    "username": "testuser",
    "createdDate": "2026-03-27T12:00:00"
  }
}
```

**실패 예시** — 중복 아이디 `409 Conflict`

```json
{
  "success": false,
  "error": {
    "status": 409,
    "errorCode": "M002",
    "message": "이미 존재하는 회원 아이디입니다."
  }
}
```

---

### 로그인

```
POST /api/v1/auth/sign-in
```

**Request Body**

```json
{
  "username": "admin",
  "password": "1234"
}
```

**Response** `200 OK`

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...",
    "tokenType": "Bearer",
    "accessTokenExpiration": 3600
  }
}
```

Refresh Token은 `Set-Cookie` 헤더로 HttpOnly 쿠키에 자동 저장됩니다. (`path: /api/v1/auth`)

**실패 예시** — 잘못된 비밀번호 `401 Unauthorized`

```json
{
  "success": false,
  "error": {
    "status": 401,
    "errorCode": "A007",
    "message": "아이디 또는 비밀번호가 일치하지 않습니다."
  }
}
```

---

### 토큰 재발급

```
POST /api/v1/auth/reissue
```

쿠키의 `refreshToken`을 자동으로 읽어 새 토큰 쌍을 발급합니다. 요청 바디 없음.

**Response** `200 OK`

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGci...(new)",
    "tokenType": "Bearer",
    "accessTokenExpiration": 3600
  }
}
```

**실패 예시** — 토큰 없음 `401 Unauthorized`

```json
{
  "success": false,
  "error": {
    "status": 401,
    "errorCode": "A006",
    "message": "토큰이 존재하지 않습니다."
  }
}
```

---

## 02. 콘텐츠 API

### 콘텐츠 목록 조회

```
GET /api/v1/contents?page=0&size=10&sort=createdDate,desc
```

인증 불필요.

| 파라미터 | 설명 | 기본값 |
|---|---|---|
| page | 페이지 번호 (0부터 시작) | 0 |
| size | 페이지당 항목 수 | 10 |
| sort | 정렬 기준 | createdDate,desc |

**Response** `200 OK`

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "첫 번째 공지사항",
        "viewCount": 11,
        "createdDate": "2026-03-27T12:00:00",
        "createdBy": "admin"
      }
    ],
    "pageNumber": 0,
    "pageSize": 10,
    "totalElements": 4,
    "totalPages": 1,
    "last": true
  }
}
```

---

### 콘텐츠 검색

```
GET /api/v1/contents/search?title=공지&createdBy=admin&page=0&size=10
```

인증 불필요. 조건은 선택 사항이며 복합 조건으로 사용 가능합니다.

| 파라미터 | 설명 | 검색 방식 |
|---|---|---|
| title | 제목 키워드 | contains (대소문자 무시) |
| createdBy | 작성자 username | equals |
| page | 페이지 번호 | — |
| size | 페이지당 항목 수 | — |

**Response** `200 OK` — 목록 조회와 동일한 포맷

---

### 콘텐츠 상세 조회

```
GET /api/v1/contents/{id}
```

인증 불필요. 조회 시 `viewCount`가 1 증가합니다.

**Response** `200 OK`

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "첫 번째 공지사항",
    "description": "관리자가 작성한 공지사항 내용입니다.",
    "viewCount": 11,
    "createdDate": "2026-03-27T12:00:00",
    "createdBy": "admin",
    "lastModifiedDate": null,
    "lastModifiedBy": null
  }
}
```

**실패 예시** — 존재하지 않는 ID `404 Not Found`

```json
{
  "success": false,
  "error": {
    "status": 404,
    "errorCode": "C001",
    "message": "콘텐츠가 존재하지 않습니다."
  }
}
```

---

### 콘텐츠 생성

```
POST /api/v1/contents
Authorization: Bearer {accessToken}
```

**Request Body**

```json
{
  "title": "새 게시물 제목",
  "description": "게시물 내용입니다."
}
```

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| title | String | Y | 최대 100자 |
| description | String | N | 최대 255자 |

**Response** `201 Created`

```json
{
  "success": true,
  "data": {
    "id": 5,
    "createdDate": "2026-03-27T13:00:00",
    "createdBy": "user1"
  }
}
```

---

### 콘텐츠 수정

```
PATCH /api/v1/contents/{id}
Authorization: Bearer {accessToken}
```

본인이 작성한 콘텐츠 또는 ADMIN만 수정 가능합니다.

**Request Body**

```json
{
  "title": "수정된 제목",
  "description": "수정된 내용입니다."
}
```

| 필드 | 타입 | 필수 | 제약 |
|---|---|---|---|
| title | String | Y | 최대 100자 |
| description | String | N | 최대 255자 |

**Response** `200 OK`

```json
{
  "success": true,
  "data": {
    "id": 5,
    "lastModifiedDate": "2026-03-27T14:00:00",
    "lastModifiedBy": "user1"
  }
}
```

**실패 예시** — 타인의 콘텐츠 수정 시도 `403 Forbidden`

```json
{
  "success": false,
  "error": {
    "status": 403,
    "errorCode": "A001",
    "message": "해당 콘텐츠에 대한 권한이 없습니다."
  }
}
```

---

### 콘텐츠 삭제

```
DELETE /api/v1/contents/{id}
Authorization: Bearer {accessToken}
```

본인이 작성한 콘텐츠 또는 ADMIN만 삭제 가능합니다.

**Response** `204 No Content`

---

## 03. 관리자 API

### 회원 역할 변경

```
PATCH /api/v1/admin/{id}/role
Authorization: Bearer {accessToken}  ← ADMIN 계정만 가능
```

**Request Body**

```json
{
  "role": "ADMIN"
}
```

| 값 | 설명 |
|---|---|
| `ADMIN` | 관리자로 승격 |
| `MEMBER` | 일반 사용자로 강등 |

**Response** `200 OK`

```json
{
  "success": true,
  "data": {
    "id": 2,
    "username": "user1",
    "lastModifiedDate": "2026-03-27T15:00:00",
    "lastModifiedBy": "admin"
  }
}
```

**실패 예시** — ADMIN이 아닌 계정으로 호출 `403 Forbidden`

```json
{
  "success": false,
  "error": {
    "status": 403,
    "errorCode": "A001",
    "message": "해당 요청에 대한 권한이 없습니다."
  }
}
```

**실패 예시** — 존재하지 않는 사용자 `404 Not Found`

```json
{
  "success": false,
  "error": {
    "status": 404,
    "errorCode": "M001",
    "message": "존재하지 않는 사용자입니다."
  }
}
```
