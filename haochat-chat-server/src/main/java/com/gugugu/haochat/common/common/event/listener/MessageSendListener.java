package com.gugugu.haochat.common.common.event.listener;

import com.gugugu.haochat.common.chat.dao.MessageDao;
import com.gugugu.haochat.common.chat.domain.dto.MsgSendMessageDTO;
import com.gugugu.haochat.common.chat.domain.entity.Message;
import com.gugugu.haochat.common.chat.domain.entity.Room;
import com.gugugu.haochat.common.chat.domain.enums.HotFlagEnum;
import com.gugugu.haochat.common.chat.service.ChatService;
import com.gugugu.haochat.common.chat.service.cache.RoomCache;
import com.gugugu.haochat.common.common.constant.MQConstant;
import com.gugugu.haochat.common.common.event.MessageSendEvent;
import com.gugugu.haochat.common.common.service.MQProducer;
import com.gugugu.haochat.common.websocket.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Objects;

@Slf4j
@Component
public class MessageSendListener {
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private MQProducer mqProducer;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, classes = MessageSendEvent.class, fallbackExecution = true)
    public void messageRoute(MessageSendEvent event) {
        Long msgId = event.getMsgId();
        mqProducer.sendSecureMsg(MQConstant.SEND_MSG_TOPIC, new MsgSendMessageDTO(msgId), msgId);
    }

    @TransactionalEventListener(classes = MessageSendEvent.class, fallbackExecution = true)
    public void handlerMsg(@NotNull MessageSendEvent event) {
        Message message = messageDao.getById(event.getMsgId());
        Room room = roomCache.get(message.getRoomId());
        if (isHotRoom(room)) {
//            openAIService.chat(message);
        }
    }

    public boolean isHotRoom(Room room) {
        return Objects.equals(HotFlagEnum.YES.getType(), room.getHotFlag());
    }

}
