package com.gugugu.haochat.chat.domain.vo.resp.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResp {

    /**
     * 发送者的信息
     */
    private UserInfo fromUser;

    /**
     * 消息体
     */
    private Message message;

    /**
     * 用户信息
     * 因为用户如果能发消息，就已经代表已上线，上线列表已经获取到，本地缓存已经生效，
     * 本地缓存了用户的姓名和头像，没必要再进行重新获取）
     */
    @Data
    @Builder
    public static class UserInfo {
        /**
         * 用户ID
         */
        private Long uid;

        /**
         * 归属地
         */
        private String place;
    }

    /**
     * 消息体
     */
    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {
        /**
         * 消息ID
         */
        private Long id;

        /**
         * 房间ID
         */
        private Long roomId;

        /**
         * 发送时间
         */
        private Date sendTime;

        /**
         * 消息类型
         */
        private Integer type;

        /**
         * 消息体
         */
        private Object body;

        /**
         * 父消息，如果没有父消息，返回的是null
         */
        private ReplyMsg reply;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReplyMsg {
        /**
         * 消息id
         */
        private Long id;
        /**
         * 用户uid
         */
        private Long uid;
        /**
         * 用户名称
         */
        private String name;
        /**
         * 消息类型 1正常文本 2.撤回消息
         */
        private Integer type;
        /**
         * 消息内容不同的消息类型，见父消息内容体
         */
        private Object body;
        /**
         * 是否可消息跳转 0否 1是
         */
        private Integer canCallback;
        /**
         * 跳转间隔的消息条数
         */
        private Integer gapCount;
    }
}
