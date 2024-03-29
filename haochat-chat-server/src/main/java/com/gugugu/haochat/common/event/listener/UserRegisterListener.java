package com.gugugu.haochat.common.event.listener;

import com.gugugu.haochat.common.event.UserRegisterEvent;
import com.gugugu.haochat.user.dao.UserDao;
import com.gugugu.haochat.user.domain.entity.User;
import com.gugugu.haochat.user.domain.enums.IdempotentEnum;
import com.gugugu.haochat.user.domain.enums.ItemEnum;
import com.gugugu.haochat.user.service.IUserBackpackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UserRegisterListener {
    @Autowired
    private IUserBackpackService userBackpackService;
    @Autowired
    private UserDao userDao;
    @Async
    @TransactionalEventListener(classes = UserRegisterEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void sendCard(UserRegisterEvent event){
        User user = event.getUser();
        userBackpackService.acquireItem(user.getId(), ItemEnum.MODIFY_NAME_CARD.getId(), IdempotentEnum.UID,user.getId().toString());
    }
    //发放徽章
    @Async
    @TransactionalEventListener(classes = UserRegisterEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void sendBadge(UserRegisterEvent event){
        User user = event.getUser();
        int registerCount = userDao.count();
        if(registerCount < 10){
            userBackpackService.acquireItem(user.getId(), ItemEnum.REG_TOP10_BADGE.getId(), IdempotentEnum.UID,user.getId().toString());
        } else if (registerCount < 100) {
            userBackpackService.acquireItem(user.getId(), ItemEnum.REG_TOP100_BADGE.getId(), IdempotentEnum.UID,user.getId().toString());
        }

    }
}
