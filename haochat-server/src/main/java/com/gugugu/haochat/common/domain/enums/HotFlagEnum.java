package com.gugugu.haochat.common.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum HotFlagEnum {
    /**
     *
     */
    NOT(0, "非热点"),
    YES(1, "热点"),
    ;

    private final Integer type;
    private final String desc;

    private static final Map<Integer, HotFlagEnum> CACHE;

    static {
        CACHE = Arrays.stream(HotFlagEnum.values()).collect(Collectors.toMap(HotFlagEnum::getType, Function.identity()));
    }

    public static HotFlagEnum of(Integer type) {
        return CACHE.get(type);
    }
}
