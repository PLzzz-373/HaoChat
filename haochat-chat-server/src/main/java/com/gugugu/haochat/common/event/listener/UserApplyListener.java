package com.gugugu.haochat.common.event.listener;

import com.gugugu.haochat.common.event.UserApplyEvent;
import com.gugugu.haochat.user.dao.UserApplyDao;
import com.gugugu.haochat.user.domain.entity.UserApply;
import com.gugugu.haochat.user.service.adapter.WSAdapter;
import com.gugugu.haochat.user.service.impl.PushService;
import com.gugugu.haochat.websocket.domain.vo.response.ws.WSFriendApply;
import com.gugugu.haochat.websocket.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class UserApplyListener {
    @Autowired
    private UserApplyDao userApplyDao;


    @Autowired
    private PushService pushService;

    @Async
    @TransactionalEventListener(classes = UserApplyEvent.class, fallbackExecution = true)
    public void notifyFriend(UserApplyEvent event) {
        UserApply userApply = event.getUserApply();
        Integer unReadCount = userApplyDao.getUnReadCount(userApply.getTargetId());
        pushService.sendPushMsg(WSAdapter.buildApplySend(new WSFriendApply(userApply.getUid(), unReadCount)), userApply.getTargetId());
    }

}
