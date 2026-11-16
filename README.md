# akive-api-java

> 학습의 모든 순간을 하나로

akive는 목표 설정부터 계획, 실행, 기록, 분석, 회고, 다음 계획까지 학습의 전 과정을 하나의 흐름으로 연결하는 Learning OS입니다.  
`akive-api-java`는 이러한 학습 흐름을 지원하는 핵심 도메인 로직과 API를 제공하는 Spring Boot 기반 백엔드 애플리케이션입니다.

## 주요 기능

| 영역 | 기능 |
| --- | --- |
| 인증 및 자격 증명 | 이메일 로그인 및 인증, 인증 토큰 재발급 및 리프레시 토큰 세션 관리, 패스워드 변경 및 재설정 |
| 회원 | 회원 가입 및 탈퇴, 개인정보 조회 및 수정, 학습 목표 추가 및 최신 목표 조회, 닉네임 중복 확인 |
| 약관 및 정책 | 약관 및 정책 문서 조회, 회원 응답 기록 및 상태 조회 |
| 학습 계획 | 학습 계획 수립 및 관리, 기간별 계획 달성률 조회 |
| 일일 로그 | 하루 일과 기록 및 관리, 기간별 시간 사용 비율/집중 시간/집중도 조회 |
| 출석 | 출석 인증 등록, 조회 및 취소 |
| 리포트 | 학습 리포트 생성 및 조회, 피드백 관리 |
| AI 코칭 | AI 코칭 대화와 메시지 관리, SSE 기반 AI 코치 응답 스트리밍 |
| 학습자 인사이트 | AI 코칭 대화 기반 학습자 인사이트 분석 및 갱신, 임베딩 생성과 벡터 검색 |
| LLM 사용량 | LLM 토큰 사용량 기록 및 회원별 일일 사용량 제한 |
| 이미지 | S3 Presigned URL 발급, 이미지 에셋과 변환본 메타데이터 관리, 미사용 이미지 정리 |
| 이메일 | SES 이메일 발송 |

## 기술 스택

- Java 21
- Spring Boot 3.5, Spring MVC, Spring Security
- MySQL, Redis, Qdrant, H2
- Spring Data JPA, Spring Data Redis
- OpenAI Java SDK
- Gradle, Docker
- AWS Secrets Manager, S3, SQS, SES, CodePipeline, CodeBuild, ECR, Elastic Beanstalk
- OpenTelemetry, Micrometer, CloudWatch
- JUnit 5, Testcontainers, ArchUnit

## 아키텍처

전체 서비스 아키텍처는 [서비스 아키텍처 다이어그램](https://app.diagrams.net/#G1dnF0-SZ_bVbfSojIOe0_7fk0rM65wiyQ#%7B%22pageId%22%3A%22l37_hNVp4Y4SPvZ6nx2S%22%7D)에서
확인할 수 있습니다.

코드는 도메인별로 구성했으며, 각 도메인에 헥사고날 아키텍처를 적용해 비즈니스 로직과 외부 시스템 연동 코드를 분리했습니다.

```text
net.akive.api
├── auth/
├── coaching/
├── dailylog/
├── plan/
├── report/
├── user/
├── ...
├── common/       # 공통 타입, 응답, 예외, 로깅 등
└── core/         # 애플리케이션 전역 설정
```

각 도메인의 기본 패키지 구조는 아래와 같습니다.

```text
<domain-name>/
├── domain/                  # 도메인 모델, 값 객체, 정책
├── application/
│   ├── provided/            # 애플리케이션이 외부에 제공하는 포트
│   ├── required/            # 외부 시스템에 요구하는 포트
│   └── service/             # 유스케이스 구현
└── adapter/
    ├── webapi/              # HTTP 진입 어댑터
    ├── persistence/         # JPA 영속성 어댑터
    ├── messaging/           # SQS 등 메시징 어댑터
    └── ...
```

주요 의존성 규칙은 아래와 같습니다.

- 의존성은 `adapter -> application -> domain` 방향을 따릅니다.
- 도메인 계층은 Spring, JPA, Hibernate에 의존하지 않습니다.
- 애플리케이션 계층은 구체적인 어댑터 대신 `required` 포트를 사용합니다.
- 웹 어댑터는 `provided` 포트를 호출합니다.
- 도메인별 패키지 간 순환 참조를 허용하지 않습니다.

이 규칙은
[`HexagonalArchitectureTest`](src/test/java/net/akive/api/architecture/HexagonalArchitectureTest.java)에서
ArchUnit으로 검증합니다.

## 로컬 개발 환경

### 준비 사항

- JDK 21
- Docker: 애플리케이션 이미지 빌드 및 Redis 통합 테스트 실행에 필요
- AWS 자격 증명: 애플리케이션에서 사용하는 AWS 리소스 접근에 필요
- SSH 클라이언트: 개발 환경의 MySQL, Redis, Qdrant에 SSH 터널링을 통해 접속하는 데 필요

### 개발 리소스 연결

로컬 애플리케이션은 개발 환경의 MySQL, Redis, Qdrant를 사용합니다. 해당 리소스는 외부에서 직접 접근할 수 없으므로,
Bastion Host를 경유하는 SSH 터널링이 필요합니다.

Termius 등의 SSH 클라이언트에서 아래와 같이 로컬 포트 포워딩을 설정합니다.

| 리소스 | 로컬 연결 주소 | 원격 포트 |
| --- | --- | --- |
| MySQL | `localhost:3306` | `3306` |
| Redis | `localhost:6379` | `6379` |
| Qdrant | `localhost:6334` | `6334` |

Bastion Host 주소, 사용자명, SSH 키, 개발 리소스의 실제 엔드포인트는 저장소에 포함하지 않고 별도로 관리합니다.

### 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

애플리케이션은 기본적으로 `8080` 포트를 사용합니다. 실행 후 헬스체크로 상태를 확인할 수 있습니다.

```bash
curl http://localhost:8080/actuator/health
```

예상 응답은 아래와 같습니다.

```json
{"status":"UP"}
```

### 설정 프로필

| 프로필 | 용도 | 주요 특성 |
| --- | --- | --- |
| 공통 | 모든 환경 | MySQL 드라이버, `ddl-auto=validate`, UTC 저장, Redis 타임아웃, 보안, AI 및 이미지 기본값 |
| `local` | 로컬 개발 | SQL 로그 활성화, SQS 및 트레이싱 비활성화 |
| `test` | 자동화 테스트 | H2 MySQL 호환 모드, `ddl-auto=create-drop`, SQS 및 트레이싱 비활성화 |
| `dev` | 개발 서버 | 개발 인프라 연결, SQL 로그 활성화, 트레이스 샘플링 비율 10% |
| `prod` | 운영 서버 | 운영 인프라 연결, SQL 로그 비활성화, 트레이스 샘플링 비율 5% |

- 설정 파일은 [`src/main/resources`](src/main/resources)에서 확인할 수 있습니다.
- 데이터베이스 접속 정보, JWT 시크릿, OpenAI API 키 등 민감한 값은 AWS Secrets Manager에서 관리합니다.

## API 문서

상세한 API 명세와 요청 및 응답 예시는[Postman API 문서](https://documenter.getpostman.com/view/15435209/2sBYHaMc6t)에서 확인할 수 있습니다.

## 인증 및 보안

- 인증이 필요한 API를 호출할 때는 `Authorization: Bearer <access-token>` 헤더에 액세스 토큰을 전달합니다.
- 액세스 토큰의 기본 만료 시간은 30분입니다.
- 리프레시 토큰은 `HttpOnly`, `Secure`, `SameSite=Lax` 속성을 가진 `__Host-akive_refresh_token` 쿠키로 전달됩니다.
- 로그인, 토큰 재발급 및 리프레시 토큰 무효화 요청에는 `X-CSRF-Protection: 1` 헤더가 필요합니다.
- 이메일 로그인 요청에는 클라이언트를 식별하기 위한 `X-Client-Instance-Id` UUID 헤더가 필요합니다.

## 테스트와 빌드

전체 테스트를 실행합니다.

```bash
./gradlew test
```

테스트 환경에서는 관계형 데이터베이스로 H2를 사용합니다.
Redis 통합 테스트는 Testcontainers로 `redis:7.2` 컨테이너를 실행하므로 Docker 데몬이 동작 중이어야 합니다.

특정 테스트만 실행할 수 있습니다.

```bash
./gradlew test --tests '*CoachingApiTest'
```

실행 가능한 JAR을 생성합니다.

```bash
./gradlew clean test bootJar
```

산출물은 `build/libs`에 생성됩니다.

Docker 이미지는 JAR 생성 후 빌드합니다.

```bash
docker build -t akive-api-java:local .
```

## 배포 및 모니터링

### 배포 방식

| 환경 | 대상 브랜치 | 배포 방식 |
| --- | --- | --- |
| 개발 | `dev` | 브랜치 변경을 감지해 CodePipeline 자동 실행 |
| 운영 | `main` | 운영 사고 방지를 위해 자동 실행하지 않으며, AWS CodePipeline에서 `변경 사항 릴리스`를 클릭해 수동 배포 |

배포 파이프라인은 아래 순서로 동작합니다.

```text
AWS CodePipeline
  -> AWS CodeBuild
  -> Gradle 테스트 및 bootJar
  -> Docker 이미지 빌드
  -> Amazon ECR 업로드
  -> Dockerrun.aws.json 생성
  -> Elastic Beanstalk 배포
```

관련 파일은 아래와 같습니다.

- [`buildspec.yml`](buildspec.yml): 테스트, 이미지 빌드 및 ECR 업로드
- [`Dockerfile`](Dockerfile): Java 21 런타임 이미지
- [`Dockerrun.aws.json.template`](Dockerrun.aws.json.template): Elastic Beanstalk 배포 명세
- [`.ebextensions`](.ebextensions): CloudWatch 에이전트 설정
- [`.platform`](.platform): 배포 후 CloudWatch 설정 복구 훅

### 모니터링

모니터링과 애플리케이션 상태 확인은 아래와 같이 구성합니다.

- 트레이스는 OpenTelemetry로 수집합니다.
- 보안 이벤트는 `/var/log/akive/security/security.log`에 Elastic Common Schema(ECS)를 따르는 JSON 형식으로 기록하며,
  CloudWatch Logs로 수집합니다.
- 애플리케이션 상태는 Actuator의 `health` 엔드포인트로 확인합니다.

## 개발 규칙

새 기능을 추가할 때 아래 원칙을 따릅니다.

1. 비즈니스 규칙과 값 객체는 해당 기능의 `domain`에 둡니다.
2. 유스케이스 진입점은 `application.provided` 포트로 정의합니다.
3. 저장소나 외부 API가 필요하면 `application.required` 포트로 정의합니다.
4. 웹 요청 처리와 데이터베이스 및 외부 서비스 연동 코드는 `adapter`에 둡니다.
5. 도메인 테스트와 유스케이스 테스트를 먼저 작성하고 필요한 어댑터 테스트를 추가합니다.
6. 변경 후 전체 테스트와 ArchUnit 아키텍처 테스트를 통과시킵니다.

## 문제 해결

### Secrets Manager 설정을 가져오지 못하는 경우

- 로컬 환경에 AWS 자격 증명이 설정되어 있는지 확인합니다.
- 해당 자격 증명이 사용하는 IAM 사용자 또는 역할에 Secrets Manager의 시크릿 값을
  조회할 권한(`secretsmanager:GetSecretValue`)이 있는지 확인합니다.

### MySQL 연결에 실패하는 경우

- Bastion Host를 경유한 SSH 터널링이 정상적으로 동작하는지 확인합니다.
- 개발 환경의 MySQL 엔드포인트가 `localhost:3306`에 매핑되어 있는지 확인합니다.
- 로컬의 `3306` 포트를 다른 프로세스가 사용하고 있지 않은지 확인합니다.

### Redis 연결에 실패하는 경우

- Bastion Host를 경유한 SSH 터널링이 정상적으로 동작하는지 확인합니다.
- 개발 환경의 Redis 포트 `6379`가 `localhost:6379`에 매핑되어 있는지 확인합니다.
- 로컬의 `6379` 포트를 다른 프로세스가 사용하고 있지 않은지 확인합니다.

### Redis 통합 테스트가 실패하는 경우

- Docker 데몬이 실행 중인지 확인합니다.
- Testcontainers가 `redis:7.2` 이미지를 내려받을 수 있는지 확인합니다.

### Qdrant 호출에 실패하는 경우

- Bastion Host를 경유한 SSH 터널링이 정상적으로 동작하는지 확인합니다.
- 개발 환경의 Qdrant gRPC 포트 `6334`가 `localhost:6334`에 매핑되어 있는지 확인합니다.
- 로컬의 `6334` 포트를 다른 프로세스가 사용하고 있지 않은지 확인합니다.
