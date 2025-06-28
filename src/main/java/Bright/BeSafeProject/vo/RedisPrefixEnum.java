package Bright.BeSafeProject.vo;

import lombok.Getter;

@Getter
public enum RedisPrefixEnum {
    REFRESH("auth:refresh"),
    BLACKLIST("auth:blacklist");

    private final String prefix;

    RedisPrefixEnum(String prefix) { this.prefix = prefix; }

    @Override
    public String toString() { return prefix; }
}
