# 마피아 서버

Spring Boot와 Java 21을 사용하여 구축된 마이크로서비스 기반 멀티플레이어 마피아 게임 서버입니다.

## 아키텍처 개요

다음과 같은 마이크로서비스로 구성된 멀티 모듈 Gradle 프로젝트입니다:

### 핵심 모듈

- **mafia-game** - Netty를 사용한 게임 로직 및 실시간 게임 서버
- **mafia-lobby** - 플레이어 로비 및 매치메이킹 서비스
- **mafia-match** - 매치 관리 및 조정 서비스
- **mafia-web** - 웹 API 및 REST 엔드포인트
- **mafia-admin** - 관리자 인터페이스 및 도구
- **mafia-data** - JPA/QueryDSL을 사용하는 공유 데이터 액세스 레이어

### 통신

- **Protocol Buffers** - 효율적인 클라이언트-서버 통신에 사용
- **Proto 정의** - `proto/` 디렉토리에 위치하며 게임, 로비, 매치, 웹 서비스용 별도 스키마 제공

## 기술 스택

- **Java 21** - 주 프로그래밍 언어
- **Spring Boot 3.5.6** - 애플리케이션 프레임워크
- **Gradle 8.14.3** - 빌드 도구 및 의존성 관리
- **Protocol Buffers 4.31.1** - 직렬화
- **Netty 4.1.127** - 고성능 네트워킹 (게임 서버)
- **Spring Data JPA** - 데이터 지속성
- **QueryDSL** - 타입 안전 SQL 쿼리
- **Redis** - 캐싱 및 세션 관리
- **MySQL/H2** - 데이터베이스 지원

## 프로젝트 구조

```
mafia-server/
├── mafia-admin/          # 관리 서비스
├── mafia-data/           # 데이터 액세스 레이어
├── mafia-game/           # 실시간 게임 서버
├── mafia-lobby/          # 로비 및 매치메이킹
├── mafia-match/          # 매치 조정
├── mafia-web/            # 웹 API 서비스
└── proto/                # Protocol Buffer 정의
    ├── game/
    ├── lobby/
    ├── match/
    └── web/
```

## 사전 요구사항

- Java 21 이상
- Gradle 8.14.3 또는 포함된 wrapper 사용
- MySQL 데이터베이스 (개발용으로는 H2)
- Redis 서버

## 프로젝트 빌드

모든 모듈 빌드:
```bash
./gradlew build
```

특정 모듈 빌드:
```bash
./gradlew :mafia-game:build
```

Protocol Buffers 생성:
```bash
./gradlew generateProto
```

## 서비스 실행

각 서비스는 Spring Boot 애플리케이션으로 독립적으로 실행할 수 있습니다:

### 게임 서버
```bash
./gradlew :mafia-game:bootRun
```

### 로비 서비스
```bash
./gradlew :mafia-lobby:bootRun
```

### 웹 API
```bash
./gradlew :mafia-web:bootRun
```

### 매치 서비스
```bash
./gradlew :mafia-match:bootRun
```

### 관리 서비스
```bash
./gradlew :mafia-admin:bootRun
```

## 테스트

모든 테스트 실행:
```bash
./gradlew test
```

특정 모듈 테스트 실행:
```bash
./gradlew :mafia-data:test
```

## 설정

각 서비스는 Spring Boot의 표준 설정 메커니즘을 통해 설정됩니다:
- `application.properties` 또는 `application.yml` 파일
- 환경 변수
- 명령줄 인수

주요 설정 영역:
- 데이터베이스 연결 설정
- Redis 설정
- 각 서비스의 네트워크 포트
- 게임별 매개변수

## 개발

이 프로젝트는 다음을 사용합니다:
- **Lombok** - 보일러플레이트 코드 감소
- **QueryDSL** - 타입 안전 쿼리 (코드 생성 활성화)
- **Protocol Buffers** - 크로스 플랫폼 통신
- **Maven 퍼블리싱** - 내부 모듈 의존성

생성된 소스는 빌드 시스템에 의해 자동으로 관리되며 버전 제어에서 제외됩니다.

### Git 커밋 컨벤션

이 프로젝트는 다음 커밋 메시지 규칙을 따릅니다:

#### 커밋 메시지 형식
```
<타입>(<모듈>): <제목>

<본문 (선택사항)>

<푸터 (선택사항)>
```

#### 커밋 타입
- **feat**: 새로운 기능 추가
- **fix**: 버그 수정
- **docs**: 문서 변경
- **style**: 코드 스타일 변경 (formatting, 세미콜론 누락 등)
- **refactor**: 코드 리팩토링 (기능 변경 없음)
- **test**: 테스트 추가 또는 수정
- **chore**: 빌드 프로세스 또는 보조 도구 변경

#### 모듈 스코프
- **game**: 게임 서버 관련
- **lobby**: 로비 서비스 관련
- **match**: 매치 서비스 관련
- **web**: 웹 API 관련
- **admin**: 관리 서비스 관련
- **data**: 데이터 레이어 관련
- **proto**: Protocol Buffer 관련
- **config**: 설정 관련

#### 예시
```bash
feat(game): 마피아 게임 로직 구현

- 플레이어 역할 배정 시스템 추가
- 투표 시스템 구현
- 게임 상태 관리 로직 추가

Closes #123
```

```bash
fix(lobby): 매치메이킹 오류 수정

플레이어가 중복으로 매치에 참가되는 문제 해결
```

```bash
docs: README에 설치 가이드 추가
```

#### 커밋 메시지 작성 가이드라인
- 제목은 50자 이내로 작성
- 제목 첫 글자는 소문자로 시작
- 제목 끝에 마침표 사용 금지
- 본문은 72자에서 줄바꿈
- 본문에서 변경사항의 이유와 방법을 설명
- 이슈 번호가 있다면 푸터에 참조

### Git 브랜치 전략 (Git Flow)

이 프로젝트는 Git Flow 브랜치 전략을 사용합니다:

#### 주요 브랜치

**영구 브랜치:**
- **main** - 프로덕션 배포용 안정적인 코드
- **develop** - 개발 통합 브랜치, 다음 릴리스를 위한 기능들이 통합됨

#### 보조 브랜치

**기능 브랜치 (Feature Branches):**
- **명명규칙**: `feature/<모듈>/<기능명>` 또는 `feature/<이슈번호>-<기능명>`
- **생성 기준**: develop 브랜치에서 분기
- **병합 대상**: develop 브랜치로 PR을 통해 병합
- **예시**:
  ```bash
  feature/game/player-role-system
  feature/lobby/matchmaking-logic
  feature/123-user-authentication
  ```

**릴리스 브랜치 (Release Branches):**
- **명명규칙**: `release/v<버전>`
- **생성 기준**: develop 브랜치에서 분기
- **용도**: 릴리스 준비 (버그 수정, 문서 업데이트)
- **병합 대상**: main과 develop 양쪽에 병합
- **예시**: `release/v1.0.0`

**핫픽스 브랜치 (Hotfix Branches):**
- **명명규칙**: `hotfix/v<버전>` 또는 `hotfix/<이슈설명>`
- **생성 기준**: main 브랜치에서 분기
- **용도**: 긴급한 프로덕션 버그 수정
- **병합 대상**: main과 develop 양쪽에 병합
- **예시**: `hotfix/v1.0.1`, `hotfix/critical-login-bug`

#### 브랜치 작업 플로우

**1. 새 기능 개발:**
```bash
# develop에서 feature 브랜치 생성
git checkout develop
git pull origin develop
git checkout -b feature/game/voting-system

# 개발 작업 후 푸시
git add .
git commit -m "feat(game): 투표 시스템 기본 구조 추가"
git push origin feature/game/voting-system

# GitHub에서 develop으로 PR 생성
```

**2. 릴리스 준비:**
```bash
# develop에서 release 브랜치 생성
git checkout develop
git checkout -b release/v1.0.0

# 버전 정보 업데이트, 문서 수정 등
git commit -m "chore: v1.0.0 릴리스 준비"

# main과 develop에 병합 후 태그 생성
git tag v1.0.0
```

**3. 핫픽스 적용:**
```bash
# main에서 hotfix 브랜치 생성
git checkout main
git checkout -b hotfix/critical-bug-fix

# 버그 수정 후 main과 develop에 병합
```

#### Pull Request 가이드라인

**PR 제목 형식:**
```
<타입>(<모듈>): <간단한 설명>
```

**PR 템플릿:**
```markdown
## 변경 사항
- 주요 변경 내용 요약

## 테스트
- [ ] 단위 테스트 통과
- [ ] 통합 테스트 통과
- [ ] 수동 테스트 완료

## 체크리스트
- [ ] 코드 리뷰 요청
- [ ] 문서 업데이트 (필요시)
- [ ] 버전 호환성 확인

## 관련 이슈
Closes #이슈번호
```

#### 브랜치 보호 규칙

**main 브랜치:**
- Direct push 금지
- PR을 통한 병합만 허용
- 최소 1명의 코드 리뷰 필수
- CI/CD 파이프라인 통과 필수

**develop 브랜치:**
- Direct push 금지
- PR을 통한 병합만 허용
- 빌드 성공 확인 필수

## 라이선스

이 프로젝트는 개인 마피아 게임 서버 구현입니다.