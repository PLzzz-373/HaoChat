package com.gugugu.haochat.chat.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pair;
import com.gugugu.haochat.chat.dao.*;
import com.gugugu.haochat.chat.domain.dto.MsgReadInfoDTO;
import com.gugugu.haochat.chat.domain.entity.*;
import com.gugugu.haochat.chat.domain.vo.member.MemberReq;
import com.gugugu.haochat.chat.domain.vo.req.*;
import com.gugugu.haochat.chat.domain.vo.resp.ChatMessageReadResp;
import com.gugugu.haochat.chat.service.ContactService;
import com.gugugu.haochat.chat.service.adapter.MemberAdapter;
import com.gugugu.haochat.chat.service.adapter.MessageAdapter;
import com.gugugu.haochat.chat.service.adapter.RoomAdapter;
import com.gugugu.haochat.chat.service.cache.RoomCache;
import com.gugugu.haochat.chat.service.cache.RoomGroupCache;
import com.gugugu.haochat.chat.service.helper.ChatMemberHelper;
import com.gugugu.haochat.chat.service.strategy.msg.AbstractMsgHandler;
import com.gugugu.haochat.chat.service.strategy.msg.MsgHandlerFactory;
import com.gugugu.haochat.chat.dao.*;
import com.gugugu.haochat.chat.domain.enums.MessageMarkActTypeEnum;
import com.gugugu.haochat.chat.domain.enums.MessageTypeEnum;
import com.gugugu.haochat.chat.domain.vo.resp.ChatMemberStatisticResp;
import com.gugugu.haochat.chat.domain.vo.resp.ChatMessageResp;
import com.gugugu.haochat.chat.service.ChatService;
import com.gugugu.haochat.chat.service.strategy.mark.AbstractMsgMarkStrategy;
import com.gugugu.haochat.chat.service.strategy.mark.MsgMarkFactory;
import com.gugugu.haochat.chat.service.strategy.msg.RecallMsgHandler;
import com.gugugu.haochat.common.annotation.RedissonLock;
import com.gugugu.haochat.common.domain.enums.NormalOrNoEnum;
import com.gugugu.haochat.common.domain.vo.req.CursorPageBaseReq;
import com.gugugu.haochat.common.domain.vo.resp.CursorPageBaseResp;
import com.gugugu.haochat.common.event.MessageSendEvent;
import com.gugugu.haochat.common.utils.AssertUtil;
import com.gugugu.haochat.user.dao.UserDao;
import com.gugugu.haochat.user.domain.entity.User;
import com.gugugu.haochat.user.domain.enums.ChatActiveStatusEnum;
import com.gugugu.haochat.user.domain.enums.RoleEnum;
import com.gugugu.haochat.user.service.IRoleService;
import com.gugugu.haochat.user.service.cache.UserCache;
import com.gugugu.haochat.websocket.domain.vo.response.ws.ChatMemberResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import java.util.*;
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
    @Autowired
    private IRoleService iRoleService;
    @Autowired
    private UserDao userDao;
    @Autowired
    private RoomGroupDao roomGroupDao;
    @Autowired
    private UserCache userCache;
    @Autowired
    private RecallMsgHandler recallMsgHandler;
    @Autowired
    private ContactService contactService;
    @Override
    @Transactional
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

    @Override
    public ChatMemberStatisticResp getMemberStatistic() {
        Long onlineNum = userCache.getOnlineNum();
        ChatMemberStatisticResp chatMemberStatisticResp = new ChatMemberStatisticResp();
        chatMemberStatisticResp.setOnlineNum(onlineNum);
        return chatMemberStatisticResp;
    }

    @Override
    @RedissonLock(key = "#uid")
    public void setMsgMark(Long uid, ChatMessageMarkReq req) {
        AbstractMsgMarkStrategy strategy = MsgMarkFactory.getStrategyNoNull(req.getMarkType());
        switch (MessageMarkActTypeEnum.of(req.getActType())){
            case MARK:
                strategy.mark(uid, req.getMsgId());
                break;
            case UN_MARK:
                strategy.unMark(uid,req.getMsgId());
                break;
        }
    }

    @Override
    public void recallMsg(Long uid, ChatMessageBaseReq req) {
        Message message = messageDao.getById(req.getMsgId());
        checkRecall(uid, message);
        //消息撤回
        recallMsgHandler.recall(uid, message);
    }

    @Override
    public CursorPageBaseResp<ChatMemberResp> getMemberPage(List<Long> memberUidList, MemberReq request) {
        Pair<ChatActiveStatusEnum, String> pair = ChatMemberHelper.getCursorPair(request.getCursor());
        ChatActiveStatusEnum activeStatusEnum = pair.getKey();
        String timeCursor = pair.getValue();
        List<ChatMemberResp> resultList = new ArrayList<>();//最终列表
        Boolean isLast = Boolean.FALSE;
        if (activeStatusEnum == ChatActiveStatusEnum.ONLINE) {//在线列表
            CursorPageBaseResp<User> cursorPage = userDao.getCursorPage(memberUidList, new CursorPageBaseReq(request.getPageSize(), timeCursor), ChatActiveStatusEnum.ONLINE);
            resultList.addAll(MemberAdapter.buildMember(cursorPage.getList()));//添加在线列表
            if (cursorPage.getIsLast()) {//如果是最后一页,从离线列表再补点数据
                activeStatusEnum = ChatActiveStatusEnum.OFFLINE;
                Integer leftSize = request.getPageSize() - cursorPage.getList().size();
                cursorPage = userDao.getCursorPage(memberUidList, new CursorPageBaseReq(leftSize, null), ChatActiveStatusEnum.OFFLINE);
                resultList.addAll(MemberAdapter.buildMember(cursorPage.getList()));//添加离线线列表
            }
            timeCursor = cursorPage.getCursor();
            isLast = cursorPage.getIsLast();
        } else if (activeStatusEnum == ChatActiveStatusEnum.OFFLINE) {//离线列表
            CursorPageBaseResp<User> cursorPage = userDao.getCursorPage(memberUidList, new CursorPageBaseReq(request.getPageSize(), timeCursor), ChatActiveStatusEnum.OFFLINE);
            resultList.addAll(MemberAdapter.buildMember(cursorPage.getList()));//添加离线线列表
            timeCursor = cursorPage.getCursor();
            isLast = cursorPage.getIsLast();
        }
        // 获取群成员角色ID
        List<Long> uidList = resultList.stream().map(ChatMemberResp::getUid).collect(Collectors.toList());
        RoomGroup roomGroup = roomGroupDao.getByRoomId(request.getRoomId());
        Map<Long, Integer> uidMapRole = groupMemberDao.getMemberMapRole(roomGroup.getId(), uidList);
        resultList.forEach(member -> member.setRoleId(uidMapRole.get(member.getUid())));
        //组装结果
        return new CursorPageBaseResp<>(ChatMemberHelper.generateCursor(activeStatusEnum, timeCursor), isLast, resultList);
    }

    @Override
    public CursorPageBaseResp<ChatMessageResp> getMsgPage(ChatMessagePageReq req, Long uid) {
        //用最后一条消息id，来限制被踢出人能看见的最大消息
        Long lastMsgId = getLastMsgId(req.getRoomId(), uid);
        CursorPageBaseResp<Message> cursorPage = messageDao.getCursorPage(req.getRoomId(), req, lastMsgId);
        if(cursorPage.isEmpty()){
            return CursorPageBaseResp.empty();
        }
        return CursorPageBaseResp.init(cursorPage, getMsgRespBatch(cursorPage.getList(),uid));
    }

    @Override
    public CursorPageBaseResp<ChatMessageReadResp> getReadPage(@Nullable Long uid, ChatMessageReadReq req) {
        Message message = messageDao.getById(req.getMsgId());
        AssertUtil.isNotEmpty(message,"消息id有误");
        AssertUtil.equal(uid,message.getFromUid(), "只能查看自己的消息");
        CursorPageBaseResp<Contact> page;
        if(req.getSearchType() == 1) {
            //已读
            page = contactDao.getReadPage(message, req);
        }else {
            page = contactDao.getUnReadPage(message, req);
        }
        if(CollectionUtil.isEmpty(page.getList())){
            return CursorPageBaseResp.empty();
        }
        return CursorPageBaseResp.init(page, RoomAdapter.buildReadResp(page.getList()));
    }

    @Override
    public Collection<MsgReadInfoDTO> getMsgReadInfo(Long uid, ChatMessageReadInfoReq req) {
        List<Message> messages = messageDao.listByIds(req.getMsgIds());
        messages.forEach(message -> {
            AssertUtil.equal(uid, message.getFromUid(), "只能查询自己发送的消息");
        });
        return contactService.getMsgReadInfo(messages).values();
    }

    @Override
    @RedissonLock(key = "#uid")
    public void msgRead(Long uid, ChatMessageMemberReq req) {
        Contact contact = contactDao.get(uid, req.getRoomId());
        if (Objects.nonNull(contact)) {
            Contact update = new Contact();
            update.setId(contact.getId());
            update.setReadTime(new Date());
            contactDao.updateById(update);
        } else {
            Contact insert = new Contact();
            insert.setUid(uid);
            insert.setRoomId(req.getRoomId());
            insert.setReadTime(new Date());
            contactDao.save(insert);
        }
    }

    private Long getLastMsgId(Long roomId, Long uid) {
        Room room = roomCache.get(roomId);
        AssertUtil.isNotEmpty(room, "房间号有误");
        if(room.isHotRoom()){
            return null;
        }
        AssertUtil.isNotEmpty(uid,"请先登录");
        Contact contact = contactDao.get(uid, roomId);
        return contact.getLastMsgId();
    }

    private void checkRecall(Long uid, Message message) {
        AssertUtil.isNotEmpty(message, "消息有误");
        AssertUtil.notEqual(message.getType(), MessageTypeEnum.RECALL.getType(), "消息无法撤回");
        boolean hasPower = iRoleService.hasPower(uid, RoleEnum.CHAT_MANAGER);
        if (hasPower) {
            return;
        }
        boolean self = Objects.equals(uid, message.getFromUid());
        AssertUtil.isTrue(self, "抱歉,您没有权限");
        long between = DateUtil.between(message.getCreateTime(), new Date(), DateUnit.MINUTE);
        AssertUtil.isTrue(between < 2, "覆水难收，超过2分钟的消息不能撤回哦~~");
    }

    private void check(ChatMessageReq req, Long uid){
        Room room = roomCache.get(req.getRoomId());
        if(uid == 1){
            return;
        }
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
