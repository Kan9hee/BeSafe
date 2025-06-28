package Bright.BeSafeProject.vo;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum SecuredPathEnum {
    API_LOG_OUT("/besafe/api/logOut"),
    API_SIGN_OUT("/besafe/api/signOut"),
    API_GET_USER_INFO("/besafe/api/getUserInfo"),
    API_GET_CURRENT_USAGE("/besafe/api/getCurrentUsage"),
    API_GET_SAFE_ROUTE("/besafe/api/getSafeRoute"),

    MAP_PAGE("/besafe/servicePage");

    private final String path;

    SecuredPathEnum(String path) { this.path = path; }

    @Override
    public String toString() { return path; }
    public static List<String> allPaths(){
        return Arrays.stream(values())
                .map(SecuredPathEnum::getPath)
                .collect(Collectors.toList());
    }
}
