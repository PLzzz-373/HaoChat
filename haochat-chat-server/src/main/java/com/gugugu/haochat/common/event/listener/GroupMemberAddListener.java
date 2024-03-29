package com.gugugu.haochat.common.event.listener;

import com.gugugu.haochat.chat.domain.entity.GroupMember;
import com.gugugu.haochat.chat.domain.entity.RoomGroup;
import com.gugugu.haochat.chat.domain.vo.req.ChatMessageReq;
import com.gugugu.haochat.chat.service.ChatService;
import com.gugugu.haochat.chat.service.adapter.MemberAdapter;
import com.gugugu.haochat.chat.service.adapter.RoomAdapter;
import com.gugugu.haochat.chat.service.cache.GroupMemberCache;
import com.gugugu.haochat.common.event.GroupMemberAddEvent;
import com.gugugu.haochat.user.dao.UserDao;
import com.gugugu.haochat.user.domain.entity.User;
import com.gugugu.haochat.user.service.cache.UserInfoCache;
import com.gugugu.haochat.user.service.impl.PushService;
import com.gugugu.haochat.websocket.domain.vo.response.ws.WSBaseResp;
import com.gugugu.haochat.websocket.domain.vo.response.ws.WSMemberChange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GroupMemberAddListener {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private UserDao userDao;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private PushService pushService;


    @Async
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendAddMsg(GroupMemberAddEvent event) {
        List<GroupMember> memberList = event.getMemberList();
        RoomGroup roomGroup = event.getRoomGroup();
        Long inviteUid = event.getInviteUid();
        User user = userInfoCache.get(inviteUid);
        List<Long> uidList = memberList.stream().map(GroupMember::getUid).collect(Collectors.toList());
        ChatMessageReq chatMessageReq = RoomAdapter.buildGroupAddMessage(roomGroup, user, userInfoCache.getBatch(uidList));
        chatService.sendMsg(chatMessageReq, User.UID_SYSTEM);
    }

    @Async
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendChangePush(GroupMemberAddEvent event) {
        List<GroupMember> memberList = event.getMemberList();
        RoomGroup roomGroup = event.getRoomGroup();
        List<Long> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
        List<Long> uidList = memberList.stream().map(GroupMember::getUid).collect(Collectors.toList());
        List<User> users = userDao.listByIds(uidList);
        users.forEach(user -> {
            WSBaseResp<WSMemberChange> ws = MemberAdapter.buildMemberAddWS(roomGroup.getRoomId(), user);
            pushService.sendPushMsg(ws, memberUidList);
        });
        //移除缓存
        groupMemberCache.evictMemberUidList(roomGroup.getRoomId());
    }

}
