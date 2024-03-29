package com.gugugu.haochat.common.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
@AllArgsConstructor
@Getter
public enum WsRespTypeEnum {
    /**
     * 连接成功
     */
    CONN_SUCCESS(0, "连接成功", String.class),

    /**
     * 登录二维码返回
     */
    LOGIN_URL(1, "登录二维码返回", String.class),

    /**
     * 用户扫描成功等待授权
     */
    LOGIN_SUBSCRIBE_SUCCESS(2, "用户订阅成功等待授权", null),

    /**
     * 用户登录成功返回用户信息
     */
    LOGIN_SUCCESS(3, "用户登录成功返回用户信息", String.class),

    /**
     * 新消息
     */
    MESSAGE(4, "新消息", String.class),

    /**
     * 上下线通知
     */
    ONLINE_OFFLINE_NOTIFY(5, "上下线通知", String.class),

    /**
     * 使前端的token失效，意味着前端需要重新登录
     */
    INVALIDATE_TOKEN(6, "使前端的token失效，意味着前端需要重新登录", null),

    /**
     * 拉黑用户
     */
    BLACK(7, "拉黑用户", String.class),

    /**
     * 消息标记
     */
    MARK(8, "消息标记", String.class),

    /**
     * 消息撤回
     */
    RECALL(9, "消息撤回", String.class),

    /**
     * 好友申请
     */
    APPLY(10, "好友申请", String.class),

    /**
     * 成员变动
     */
    MEMBER_CHANGE(11, "成员变动", String.class),

    /**
     * 更新上线列表
     */
    LIMIT_CONCURRENT_LOGIN(12, "同时在线设备已经达到上限，请先退出后再进行操作吧~",List .class),
    ;

    private final Integer type;
    private final String desc;
    private final Class<?> dataClass;

    private static final Map<Integer, WsRespTypeEnum> CACHE;

    static {
        CACHE = Arrays.stream(WsRespTypeEnum.values()).collect(Collectors.toMap(WsRespTypeEnum::getType, Function.identity()));
    }

    public static WsRespTypeEnum of(Integer type) {
        return CACHE.get(type);
    }
}
