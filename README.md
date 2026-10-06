# 🍃 Web B Backend

SSUMC 11기 Web 스터디 B조 백엔드 저장소

## 👥 Member

| 뚜이 | 에단 | 민스 | 페트릭 | 스텔라 |
| :---: | :---: | :---: | :---: | :---: |
| [임도현](https://github.com/dlaehgus1112) | [이아린](https://github.com/ethan0587) | [김민성](https://github.com/minseongid) | [노형원](https://github.com/20233017-RHW) | [이지현](https://github.com/jhyunniee) |

<br/>

## ⭐️ 스터디 규칙

✅ 워크북 노션 채우기 <br />
✅ 스터디 전까지 해당 주차 PR 올리기

<br/>

## 🌳 branch 규칙

```text
main
└─ 닉네임/main
   └─ 닉네임/#이슈번호
```

1. `닉네임/main` 브랜치가 각자의 기본 브랜치입니다. PR은 저장소의 `main`이 아닌 자신의 `닉네임/main` 브랜치로 보냅니다.
2. 매주 워크북, 실습, 미션은 `닉네임/main`을 base로 `닉네임/#이슈번호` 브랜치를 만들어 작업합니다.
3. 스터디원의 approve를 받으면 스터디 중에 PR을 머지하고 작업 브랜치를 삭제합니다.

## 📂 디렉터리 규칙

워크북에서 안내한 Spring Boot 프로젝트는 저장소의 `study/`에 두고 작업합니다. `src/main`은 애플리케이션 코드, `src/test`는 테스트 코드입니다.

```text
PE_Web_B_BE/
├─ .github/
│  ├─ ISSUE_TEMPLATE/
│  └─ PULL_REQUEST_TEMPLATE.md
├─ README.md
└─ study/
   ├─ build.gradle
   ├─ gradlew
   ├─ gradlew.bat
   └─ src/
      ├─ main/
      │  ├─ java/
      │  └─ resources/
      └─ test/
         └─ java/
```

Git 저장소의 루트는 `PE_Web_B_BE/`입니다. `study/` 안에 별도의 Git 저장소를 만들지 않습니다.

<br/>

## 🔖 커밋 컨벤션

| Message | 설명 |
| :---: | :--- |
| `mission` | 미션 수행 |
| `practice` | 실습 수행 |
| `workbook` | 워크북 정리 |
| `refactor` | 코드 리팩토링 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정 |
| `comment` | 주석 추가 및 변경 |
| `remove` | 파일 혹은 폴더 삭제 |
| `chore` | 기타 변경 사항 |

```text
[week주차/종류] 작업 내용
예: [week3/mission] 카테고리별 도서 조회 및 대여 기록 생성
```

## 🚀 작업 순서

1. **이슈 확인**
   - 해당 주차 이슈를 확인합니다.
2. **원격과 자신의 브랜치 동기화**
   - `닉네임/main` 브랜치를 원격 기준으로 최신 상태로 유지합니다.
3. **브랜치 생성 후 코드 작업**
   - `닉네임/main`에서 `닉네임/#이슈번호` 브랜치를 만들고 `study/`에서 작업합니다.
4. **Gradle 빌드 및 확인**
   - `study/`에서 Windows는 `./gradlew.bat clean build`, macOS/Linux는 `./gradlew clean build`를 실행합니다.
   - API 미션은 워크북 안내에 따라 Swagger UI 또는 Postman으로 요청과 응답을 확인합니다.
5. **PR 올리기**
   - PR 템플릿에 맞게 작성하고 **`닉네임/main` ← `닉네임/#이슈번호`** 방향으로 올립니다.
6. **마감 기한**
   - 스터디 전날 23:59까지 제출합니다.
