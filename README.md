# Commerce Team project

spring 3기 1조입니다.
Commerce Team project 제출합니다!
조원: 권지원, 박경화, 배강혁, 문혜린, 임하은, 정은지

-----

##  :pouting_cat: 1. 프로젝트 소개

commerce 프로젝트는 고객, 상품, 주문 데이터를 관리합니다. 이를 위해 관리자는 관리자 페이지를 사용해서 데이터에 접근할 수 있습니다. 
다량의 데이터가 이용되는 경우를 대비해 검색, 정렬, 페이징 기능을 함께 구현했습니다.
관리자는 엔티티 정보를 체계적으로 관리할 수 있습니다.
상품, 주문에 대해 생성, 수정, 삭제할 수 있으며, 리뷰와 고객에 대해 수정, 삭제할 수 있습니다.
관리자도 역할에 따라 승인, 거부 등의 추가 기능을 가집니다. 

※ 본 프로젝트는 `REST API 기반 서버`로, 별도의 View 화면은 제공하지 않지만 wireframe 에서 화면이 어떻게 동작하는지 확인할 수 있습니다.

### :hatched_chick: API 명세서

### :hatched_chick: ERD

### :hatched_chick: 트러블슈팅

### :hatched_chick: 마일스톤

| 날짜    | 목표                     | 세부 내용                                         |
|-------|------------------------|-----------------------------------------------|
| Day 1 | API 작성, CRUD 틀 시작             | API 명세서와 각 도메인 CRUD 작성                                    |
| Day 2 | CRUD 구현 후 수정          | CRUD 구현 완료 후 피드백 반영                         |
| Day 3 | merge   | domain merge 후 문제 해결, postman 에서 실행 |
| Day 4 | 업그레이드               | 페이징, 정렬, review, 접근제어                        |
| Day 5 | 문서 완성               | API 명세서 최종 작성, README 작성, PPT, 시연영상          |
| Day 6 | 발표 정리               | 발표 마지막 정리 준비           |

### :hatched_chick: WireFrame 화면
{영상}

-----

## :crying_cat_face: 2. 주요 기능
1. **Admin 기능**
```
인증
 - 관리자 회원가입
 - JWT 기반 로그인
 - 잘못된 입력에 대한 유효성 검증 및 오류 메시지 제공

기능
 - 관리자 단건 조회
 - 전체 조회(총관리자 전용)
 - 관리자 가입 승인, 거절 처리 
 - 관리자 정보 수정
 - 관리자 상태 (활성, 비활성, 정지 등) 변경
 - 관리자 역할 변경
```

2. **Customer 기능**
```
인증
 - 고객 회원가입
 - JWT 기반 로그인
 - 잘못된 입력에 대한 유효성 검증 및 오류 메시지 제공

기능
 - 관리자 상태 (활성, 비활성, 정지 등) 변경
 - 고객 단건 조회 (관리자 전용)
 - 전체 조회 가능(관리자 전용)
```

3. **Order 기능**
```
 - 고객이 직접 주문 생성 가능
 - 관리자가 대리 주문 생성 가능

 - 관리자와 고객은 서로 다른 주문 조회 화면(응답)을 사용
 - 고객: 본인의 주문만 조회 가능
 - 관리자: 전체 주문 조회 가능

 - 주문 상태 변경 (예: 주문 완료, 취소, 배송 완료 등)
 - 주문 취소 시 상품 재고 자동 복구
```

4. **Product 기능**
```
 - 상품 생성, 수정, 삭제, 상태변경 가능 (관리자 전용)
 - 상품 조회 가능 (비회원 기능)
   해당 상품의 최신 리뷰 3개도 함께 조회 가능
   ```

5. **Review 기능**
```
 - 해당 상품을 주문한 고객만 리뷰 생성 가능 
 - 리뷰 조회 가능 (비회원 기능)

 - 리뷰 삭제 가능 (관리자 전용)
```


-----



## :pouting_cat: 3. 빠른 시작 

### :hatched_chick: 사전 요구사항
- Java 17 이상
- MySQL 8.x
- Gradle (프로젝트에 포함된 Gradle Wrapper 사용 가능)
- Git

IDE는 IntelliJ IDEA, VSCode 등 자유 선택 가능

### :hatched_chick: 설치 방법

1. **저장소 클론**

git bash
```
git clone https://github.com/gkdmsdla/commerce.git 
cd commerce
```

2. **데이터 베이스 연결**

MySQL 접속 후 아래 명령어 실행

SQL
```
CREATE DATABASE commerce;
```

3. **환경 설정**

src/main/resoures/aplication.properties
```
spring.datasource.url=jdbc:mysql://localhost:3306/commerce
spring.datasource.username={본인 SQL 계정}
spring.datasource.password={본인 SQL 비밀번호}
spring.jpa.hibernate.ddl-auto=update
```

4. **실행**

서버 기본 포트
```
http://localhost:8080
```


5. **API 테스트**

본 프로젝트는 REST API 서버로, 브라우저가 아닌 API 테스트 도구를 통해 확인합니다.
`Postman 사용 권장`

API 명세서를 확인하세요
```
https://documenter.getpostman.com/view/{ } 
```

-----

## :scream_cat: 4. 저장소 구조
global -> 공통 기능 (보안, 예외처리, 설정, 응답 포맷)

```

com.example.commerce
├── admin 
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
│
├── customer 
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
│
├── global 
│   ├── common
│   │   ├── BaseEntity
│   │   ├── CommonResponseDTO
│   │   ├── CommonResponseHandler
│   │   └── SuccessCode
│   │
│   ├── config
│   │   ├── InitialAdminConfig
│   │   ├── PasswordEncoder
│   │   └── SecurityConfig
│   │
│   ├── exception
│   │   ├── ErrorCode
│   │   ├── ErrorResponse
│   │   ├── GlobalExceptionHandler
│   │   └── ServiceException
│   │
│   └── security
│       ├── entity
│       │   └── RefreshToken
│       │
│       ├── repository
│       │   └── RefreshTokenRepository
│       │
│       ├── AdminUserDetails
│       ├── CustomerUserDetails
│       ├── CustomerUserDetailsService
│       ├── JwtFilter
│       ├── JwtUtil
│       └── UserPrincipal
│
├── order 
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
│
├── product 
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
│
├── review 
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
│
└── CommerceApplication

```

-----

## :smiley_cat: 5. 기술 스택

- Java 17
- Spring Boot
- Spring Data JPA
- MySQL
- Gradle
- Postman (API 테스트)


-----

## :kissing_cat: 6. 인증 방식

- `JWT Token` 인증 방식 사용
- 로그인 성공 시 Token 생성
- PreAuthorization 과 Spring Security 를 통한 작성자 검증
 (역할 별 가능한 기능 제한) 

-----


## :joy_cat: 7. 예외 처리, 공통 응답

- @RestControllerAdvice를 활용한 전역 예외 처리
- `ErrorCode Enum`을 활용한 통일된 에러 응답 형식
- DTO 유효성 검증 실패 시 `커스텀 에러 메시지` 반환
- 요청 응답 성공 시에도 `SuccessCode Enum` 을 활용한 통일된 응답 반환


# :sunflower: :ear_of_rice: :ear_of_rice: :cat2: :sunflower: :herb: :sunflower: :sunflower: :hatching_chick: :herb: :ear_of_rice: :ear_of_rice: :ear_of_rice: :herb: :sunflower: :sunflower: :sunflower: :leopard:
