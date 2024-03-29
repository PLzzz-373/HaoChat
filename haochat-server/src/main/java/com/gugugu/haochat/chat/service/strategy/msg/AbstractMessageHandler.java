package com.gugugu.haochat.chat.service.strategy.msg;

import com.gugugu.haochat.chat.domain.entity.Message;
import com.gugugu.haochat.chat.domain.vo.req.message.ChatMessageReq;
import com.gugugu.haochat.chat.domain.vo.resp.message.ChatMessageResp;
import com.gugugu.haochat.chat.service.strategy.msg.factory.MessageHandlerFactory;
import com.gugugu.haochat.common.domain.enums.MessageTypeEnum;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;

public abstract class AbstractMessageHandler<RESP> {

    private Class<RESP> bodyClass;

    /**
     * 初始化注册策略
     */
    @PostConstruct
    private void init() {
        ParameterizedType genericSuperclass = (ParameterizedType) this.getClass().getGenericSuperclass();
        this.bodyClass = (Class<RESP>) genericSuperclass.getActualTypeArguments()[0];
        MessageHandlerFactory.register(getMessageTypeEnum().getType(), (AbstractMessageHandler<Object>) this);
    }

    /**
     * 消息类型
     *
     * @return 消息类型
     */
    abstract MessageTypeEnum getMessageTypeEnum();

    /**
     * 校验消息——保存前校验
     *
     * @param chatMessageReq 请求消息体
     * @param uid            发送消息的用户ID
     */
    public abstract void checkMessage(ChatMessageReq chatMessageReq, Long uid);

    /**
     * 保存消息
     *
     * @param message        消息
     * @param chatMessageReq 请求消息体
     */
    public abstract void saveMessage(Message message, ChatMessageReq chatMessageReq);

    /**
     * 构建响应消息体
     *
     * @param message 消息对象
     * @param builder 构造器
     * @return 响应消息体
     */
    public abstract ChatMessageResp.Message buildChatMessageResp(Message message, ChatMessageResp.Message.MessageBuilder builder);

    /**
     * 构建消息返回体对象
     *
     * @param message 消息体对象
     * @return 消息体对象
     */
    public abstract RESP buildResponseBody(Message message);

    /**
     * 被回复时——展示的消息
     *
     * @param message 消息体
     * @return 被回复时——展示的消息
     */
    public abstract String showInReplyMessage(Message message);

    /**
     * 会话列表——展示的消息
     *
     * @param message 消息体
     * @return 会话列表——展示的消息
     */
    public abstract String showInContactMessage(Message message);
}
