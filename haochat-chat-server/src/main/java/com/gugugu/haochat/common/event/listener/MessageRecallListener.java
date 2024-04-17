package com.gugugu.haochat.common.event.listener;

import com.gugugu.haochat.chat.domain.dto.ChatMsgRecallDTO;
import com.gugugu.haochat.chat.service.cache.MsgCache;
import com.gugugu.haochat.common.event.MessageRecallEvent;
import com.gugugu.haochat.user.service.adapter.WSAdapter;
import com.gugugu.haochat.user.service.impl.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
@Slf4j
@Component
public class MessageRecallListener {
    @Autowired
    private MsgCache msgCache;
    @Autowired
    private PushService pushService;
    @Async
    @TransactionalEventListener(classes = MessageRecallEvent.class, fallbackExecution = true)
    public void evictMsg(MessageRecallEvent event){
        ChatMsgRecallDTO recallDto = event.getRecallDTO();
        msgCache.evictMsg(recallDto.getMsgId());
    }
    @Async
    @TransactionalEventListener(classes = MessageRecallEvent.class, fallbackExecution = true)
    public void sendToAll(MessageRecallEvent event) {
        pushService.sendPushMsg(WSAdapter.buildMsgRecall(event.getRecallDTO()));
    }

}
