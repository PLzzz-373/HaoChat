package com.gugugu.haochat.common.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.gugugu.haochat.common.chat.dao.*;
import com.gugugu.haochat.common.chat.domain.entity.*;
import com.gugugu.haochat.common.chat.domain.vo.req.ChatMessageReq;
import com.gugugu.haochat.common.chat.domain.vo.resp.ChatMessageResp;
import com.gugugu.haochat.common.chat.service.ChatService;
import com.gugugu.haochat.common.chat.service.adapter.MessageAdapter;
import com.gugugu.haochat.common.chat.service.cache.RoomCache;
import com.gugugu.haochat.common.chat.service.cache.RoomGroupCache;
import com.gugugu.haochat.common.chat.service.strategy.msg.AbstractMsgHandler;
import com.gugugu.haochat.common.chat.service.strategy.msg.MsgHandlerFactory;
import com.gugugu.haochat.common.common.domain.enums.NormalOrNoEnum;
import com.gugugu.haochat.common.common.event.MessageSendEvent;
import com.gugugu.haochat.common.common.utils.AssertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private RoomCache roomCache;
    @Autowired
    private ContactDao contactDao;
    @Autowired
    private MessageMarkDao messageMarkDao;
    @Autowired
    private RoomFriendDao roomFriendDao;
    @Autowired
    private RoomGroupCache roomGroupCache;
    @Autowired
    private GroupMemberDao groupMemberDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public Long sendMsg(ChatMessageReq req, Long uid) {
        check(req, uid);
        AbstractMsgHandler<?> msgHandler = MsgHandlerFactory.getStrategyNoNull(req.getMsgType());
        Long msgId = msgHandler.checkAndSaveMsg(req, uid);
        //发布消息发送事件
        applicationEventPublisher.publishEvent(new MessageSendEvent(this,msgId));
        return msgId;
    }
    @Override
    public ChatMessageResp getMsgResp(Message message, Long receiveUid) {
        return CollUtil.getFirst(getMsgRespBatch(Collections.singletonList(message), receiveUid));
    }

    @Override
    public ChatMessageResp getMsgResp(Long msgId, Long recUid) {
        Message msg = messageDao.getById(msgId);
        return getMsgResp(msg, recUid);
    }

    private void check(ChatMessageReq req, Long uid){
        Room room = roomCache.get(req.getRoomId());
        if(room.isHotRoom()){
            return;
        }
        if(room.isRoomFriend()){
            RoomFriend roomFriend = roomFriendDao.getByRoomId(req.getRoomId());
            AssertUtil.equal(NormalOrNoEnum.NORMAL.getStatus(), roomFriend.getStatus(), "您已经被对方拉黑");
            AssertUtil.isTrue(uid.equals(roomFriend.getUid1()) || uid.equals(roomFriend.getUid2()), "您已经被对方拉黑");
        }
        if(room.isRoomGroup()){
            RoomGroup roomGroup = roomGroupCache.get(req.getRoomId());
            GroupMember member = groupMemberDao.getMember(roomGroup.getId(), uid);
            AssertUtil.isNotEmpty(member, "您已经被移除该群");
        }
    }
    public List<ChatMessageResp> getMsgRespBatch(List<Message> messages, Long receiveUid) {
        if (CollectionUtil.isEmpty(messages)) {
            return new ArrayList<>();
        }
        //查询消息标志
        List<MessageMark> msgMark = messageMarkDao.getValidMarkByMsgIdBatch(messages.stream().map(Message::getId).collect(Collectors.toList()));
        return MessageAdapter.buildMsgResp(messages, msgMark, receiveUid);
    }
}
