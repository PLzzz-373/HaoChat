package com.gugugu.haochat.common.constant;

public class RedisKeyConst {

    /**
     * 根前缀
     */
    private static final String BASE_KEY = "haochat:";

    /**
     * 登录码
     */
    public static final String LOGIN_CODE = "login:code";

    /**
     * 在线用户列表
     */
    public static final String ONLINE_UID_ZET = "online";

    /**
     * 离线用户列表
     */
    public static final String OFFLINE_UID_ZET = "offline";

    /**
     * 热门房间列表
     */
    public static final String HOT_ROOM_ZET = "hotRoom";

    /**
     * 用户信息
     */
    public static final String USER_INFO_STRING = "userInfo:uid_%d";

    /**
     * 用户总数
     */
    public static final String USER_TOTAL_COUNT_STRING = "user:total_count:";

    /**
     * 房间详情
     */
    public static final String ROOM_INFO_STRING = "roomInfo:roomId_%d";

    /**
     * 好友详情
     */
    public static final String FRIEND_INFO_STRING = "friendInfo:roomId_%d";

    /**
     * 群组详情
     */
    public static final String GROUP_INFO_STRING = "groupInfo:roomId_%d";

    /**
     * 用户token存放
     */
    public static final String USER_TOKEN_STRING = "userToken:uid_%d";

    /**
     * 用户的信息更新时间
     */
    public static final String USER_MODIFY_STRING = "userModify:uid_%d";

    /**
     * 用户的信息汇总
     */
    public static final String USER_SUMMARY_STRING = "userSummary:uid_%d";

    /**
     * 用户的会话列表
     */
    public static final String USER_CONTACT_LIST = "contact:uid_%d";

    /**
     * 用户GPT聊天次数
     */
    public static final String USER_CHAT_NUM = "useChatGPTNum:uid_%d";

    public static final String USER_CHAT_CONTEXT = "useChatGPTContext:uid_%d_roomId_%d";

    /**
     * 保存Open id
     */
    public static final String OPEN_ID_STRING = "openid:%s";

    /**
     * 用户上次使用GLM使用时间
     */
    public static final String USER_GLM2_TIME_LAST = "userGLM2UseTime:uid_%d";

    public static String getKey(String key, Object... objects) {
        return BASE_KEY + String.format(key, objects);
    }

}
