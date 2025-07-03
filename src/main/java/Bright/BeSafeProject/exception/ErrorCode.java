package Bright.BeSafeProject.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    TOKEN_NOT_FOUND(401,"액세스 혹은 리프레시 토큰이 누락되었습니다."),
    EMAIL_INFO_MISSING_TOKEN(401,"토큰 내 사용자 정보가 포함되어 있지 않습니다."),
    EXPIRED_TOKEN(401,"만료된 토큰입니다."),

    ACCOUNT_SAVE_FAILED(500,"회원가입에 실패했습니다."),
    BLANK_PASSWORD(400,"비밀번호 데이터가 입력되지 않았습니다."),
    WRONG_PASSWORD(400,"잘못된 비밀번호가 입력되었습니다."),
    ACCOUNT_ALREADY_EXISTS(400,"이미 가입된 사용자입니다."),
    ACCOUNT_NOT_FOUND(404,"사용자 데이터를 찾을 수 없습니다."),
    ACCOUNT_DELETE_FAILED(500,"회원탈퇴에 실패했습니다."),

    USAGE_HISTORY_SAVE_FAILED(500,"사용 이력 저장에 실패했습니다."),
    USAGE_HISTORY_CALL_FAILED(500,"사용 이력을 불러오는데 실패했습니다."),

    LIGHT_COLLECTION_UPDATE_FAILED(500,"가로등 위치 데이터 업데이트에 실패했습니다."),
    LIGHT_COLLECTION_SEARCH_FAILED(500,"가로등 위치 데이터 검색에 실패했습니다."),

    NOT_SUPPORTED_PLATFORM(400,"지원하지 않는 플랫폼입니다."),

    INTERNAL_API_VALUE_ERROR(502,"API 호출에 실패했습니다."),
    INTERNAL_API_SERVER_ERROR(502,"API 서버가 응답하지 않습니다."),
    API_RESULT_IS_EMPTY(502,"API 호출 결과가 없습니다."),

    INTERNAL_SERVER_ERROR(500, "서버에 문제가 발생했습니다. 관리자에게 문의 바랍니다.");

    private final int status;
    private final String errorMessage;

    ErrorCode(int status,String errorMessage){
        this.status=status;
        this.errorMessage=errorMessage;
    }
}
