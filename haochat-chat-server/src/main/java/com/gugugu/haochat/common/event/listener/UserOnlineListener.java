package com.gugugu.haochat.common.event.listener;

import com.gugugu.haochat.common.event.UserOnlineEvent;
import com.gugugu.haochat.user.dao.UserDao;
import com.gugugu.haochat.user.domain.entity.User;
import com.gugugu.haochat.user.domain.enums.UserActiveStatusEnum;
import com.gugugu.haochat.user.service.IpService;
import com.gugugu.haochat.user.service.adapter.WSAdapter;
import com.gugugu.haochat.user.service.cache.UserCache;
import com.gugugu.haochat.user.service.impl.PushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
@Component
public class UserOnlineListener {
    @Autowired
    private IpService ipService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserCache userCache;
    @Autowired
    private PushService pushService;
    @Autowired
    private WSAdapter wsAdapter;
    @Async
    @TransactionalEventListener(classes = UserOnlineEvent.class, phase = TransactionPhase.AFTER_COMMIT,fallbackExecution = true)
    public void saveDB(UserOnlineEvent event){
        User user = event.getUser();
        User update = new User();
        update.setId(user.getId());
        update.setLastOptTime(user.getLastOptTime());
        update.setIpInfo(user.getIpInfo());
        update.setActiveStatus(UserActiveStatusEnum.ONLINE.getStatus());
        userDao.updateById(update);
        //用户ip详情的解析
        ipService.refreshIpDetailAsync(user.getId());
    }
    @Async
    @EventListener(classes = UserOnlineEvent.class)
    public void saveRedisAndPush(UserOnlineEvent event) {
        User user = event.getUser();
        userCache.online(user.getId(), user.getLastOptTime());
        //推送给所有在线用户，该用户登录成功
        pushService.sendPushMsg(wsAdapter.buildOnlineNotifyResp(event.getUser()));
    }
}
