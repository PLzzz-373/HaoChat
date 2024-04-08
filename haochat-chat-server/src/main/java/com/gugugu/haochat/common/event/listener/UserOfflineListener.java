package com.gugugu.haochat.common.event.listener;

import com.gugugu.haochat.common.event.UserOfflineEvent;
import com.gugugu.haochat.user.dao.UserDao;
import com.gugugu.haochat.user.domain.entity.User;
import com.gugugu.haochat.user.domain.enums.ChatActiveStatusEnum;
import com.gugugu.haochat.user.service.adapter.WSAdapter;
import com.gugugu.haochat.user.service.cache.UserCache;
import com.gugugu.haochat.websocket.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
@Slf4j
public class UserOfflineListener {
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserCache userCache;
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private WSAdapter wsAdapter;

    /**
     * 更新缓存层数据库
     *
     * @param event 用户下线事件参数
     */
    @Async
    @EventListener(classes = UserOfflineEvent.class)
    public void updateCacheDB(UserOfflineEvent event) {
        User user = event.getUser();
        // 在redis缓存中删除那个uid
        userCache.offline(user.getId(), user.getLastOptTime());
        //用户下线事件推送
        webSocketService.sendToAllOnline(wsAdapter.buildOfflineNotifyResp(user),user.getId());
    }

    /**
     * 更新持久层数据库
     *
     * @param event 用户下线事件参数
     */
    @Async
    @EventListener(classes = UserOfflineEvent.class)
    public void updatePersistenceDB(UserOfflineEvent event) {
        User user = event.getUser();
        User update = new User();
        update.setId(user.getId());
        update.setLastOptTime(user.getLastOptTime());
        update.setActiveStatus(ChatActiveStatusEnum.OFFLINE.getStatus());
        userDao.updateById(update);
    }
}