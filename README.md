# BeSafe

야간 안전 경로 정보 제공 서비스

## 개발 환경
- Springboot, Spring JPA, Spring Security
- Java
- Mysql

## 실행 화면
|Screen #1|Screen #2|
|:---:|:---:|
|<img src="https://github.com/Kan9hee/BeSafe/blob/master/startScreen.PNG" width="400"/>|<img src="https://github.com/Kan9hee/BeSafe/blob/master/navigation.PNG" width="400"/>|

## 주요 기능

### OAuth2 소셜로그인
- 사용자 정보
    - 회원가입, 로그인, 로그아웃, 사용 이력 기록 및 재검색
    - 카카오 연동 로그인
        - 토큰을 통해 사용자 정보 조회

### 경로 내 거리등 위치 도출
- 거리등 위치 관리
    - 공공데이터 기반 위치정보 확보
    - 일정 주기마다 데이터 업데이트
- 경로 시각화
    - Tmap 기반 경로 검색 API 이용
    - 경로 결과 내 거리등 위치 및 조명 범위 시각화

## 참고자료
- https://tmapapi.tmapmobility.com
- https://www.data.go.kr
