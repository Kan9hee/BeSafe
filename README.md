# BeSafe

야간 안전 경로 정보 제공 서비스

## 개발 환경
- Springboot, Spring WebFlux, Spring Security
- Java
- Mysql, MongoDB, Redis

## 주요 기능

### OAuth2 소셜로그인
|Flow|
|:---:|
|<img src="https://github.com/Kan9hee/BeSafe/blob/master/besafe_logIn_flow.png" width="800"/>|

|Screen|
|:---:|
|<img src="https://github.com/Kan9hee/BeSafe/blob/master/besafe_login.png" width="800"/>|
- 사용자 정보
    - 회원가입, 로그인, 로그아웃, 서비스 사용 이력 관리
    - 구글 및 카카오 연동 로그인
        - 토큰을 통해 사용자 정보 조회

### 경로 내 거리등 위치 도출
|Flow-getData|
|:---:|
|<img src="https://github.com/Kan9hee/BeSafe/blob/master/besafe_getData_flow.png" width="800"/>|

|Flow-scheduling|
|:---:|
|<img src="https://github.com/Kan9hee/BeSafe/blob/master/besafe_scheduling_flow.png" width="800"/>|

|Screen|
|:---:|
|<img src="https://github.com/Kan9hee/BeSafe/blob/master/besafe_service.png" width="800"/>|
- 거리등 위치 관리
    - 공공데이터 기반 위치정보 확보
    - 일정 주기마다 데이터 업데이트
    - GeoJson을 통해 경로 주변 20m 내 거리등 데이터 도출
- 경로 시각화
    - Tmap 기반 경로 검색 API 이용
    - 도출된 경로 및 거리등 위치, 조명 범위 시각화

## 참고자료
- https://tmapapi.tmapmobility.com
- https://www.data.go.kr
