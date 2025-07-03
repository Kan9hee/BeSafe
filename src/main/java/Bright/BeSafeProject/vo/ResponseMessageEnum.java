package Bright.BeSafeProject.vo;

public enum ResponseMessageEnum {
    JOIN_SUCCESS("가입 완료"),
    LOGIN_SUCCESS("로그인 완료"),
    LOGOUT_SUCCESS("로그아웃 완료"),
    WITHDRAW_ACCOUNT_SUCCESS("회원 탈퇴 완료");

    private final String prefix;

    ResponseMessageEnum(String prefix) { this.prefix = prefix; }
}
