package com.gugugu.haochat.common.domain.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum ItemTypeEnum {
    /**
     *
     */
    MODIFY_NAME_CARD(1, "改名卡"),
    BADGE(2, "徽章"),
    ;

    private final Integer type;
    private final String desc;

    private static final Map<Integer, ItemTypeEnum> CACHE;

    static {
        CACHE = Arrays.stream(ItemTypeEnum.values()).collect(Collectors.toMap(ItemTypeEnum::getType, Function.identity()));
    }

    public static ItemTypeEnum of(Integer type) {
        return CACHE.get(type);
    }
}
