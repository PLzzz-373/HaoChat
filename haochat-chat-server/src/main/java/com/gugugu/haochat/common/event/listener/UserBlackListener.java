package com.gugugu.haochat.common.event.listener;

import com.gugugu.haochat.chat.dao.MessageDao;
import com.gugugu.haochat.common.event.UserBlackEvent;
import com.gugugu.haochat.user.service.cache.UserCache;
import com.gugugu.haochat.websocket.domain.enums.WSRespTypeEnum;
import com.gugugu.haochat.websocket.domain.vo.response.ws.WSBaseResp;
import com.gugugu.haochat.websocket.domain.vo.response.ws.WSBlack;
import com.gugugu.haochat.websocket.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserBlackListener {
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private UserCache userCache;

    @Async
    @EventListener(classes = UserBlackEvent.class)
    public void refreshRedis(UserBlackEvent event) {
        userCache.evictBlackMap();
        userCache.remove(event.getUser().getId());
    }

    @Async
    @EventListener(classes = UserBlackEvent.class)
    public void deleteMsg(UserBlackEvent event) {
        messageDao.invalidByUid(event.getUser().getId());
    }

    @Async
    @EventListener(classes = UserBlackEvent.class)
    public void sendPush(UserBlackEvent event) {
        Long uid = event.getUser().getId();
        WSBaseResp<WSBlack> resp = new WSBaseResp<>();
        WSBlack black = new WSBlack(uid);
        resp.setData(black);
        resp.setType(WSRespTypeEnum.BLACK.getType());
        webSocketService.sendToAllOnline(resp, uid);
    }


}
