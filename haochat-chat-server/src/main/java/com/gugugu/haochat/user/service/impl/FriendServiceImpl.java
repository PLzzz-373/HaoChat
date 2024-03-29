package com.gugugu.haochat.user.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.gugugu.haochat.chat.domain.entity.RoomFriend;
import com.gugugu.haochat.chat.service.ChatService;
import com.gugugu.haochat.chat.service.RoomService;
import com.gugugu.haochat.chat.service.adapter.MessageAdapter;
import com.gugugu.haochat.common.annotation.RedissonLock;
import com.gugugu.haochat.common.domain.vo.req.CursorPageBaseReq;
import com.gugugu.haochat.common.domain.vo.req.PageBaseReq;
import com.gugugu.haochat.common.domain.vo.resp.CursorPageBaseResp;
import com.gugugu.haochat.common.domain.vo.resp.PageBaseResp;
import com.gugugu.haochat.common.event.UserApplyEvent;
import com.gugugu.haochat.user.dao.UserApplyDao;
import com.gugugu.haochat.user.dao.UserDao;
import com.gugugu.haochat.user.dao.UserFriendDao;
import com.gugugu.haochat.user.domain.entity.User;
import com.gugugu.haochat.user.domain.entity.UserApply;
import com.gugugu.haochat.user.domain.entity.UserFriend;
import com.gugugu.haochat.user.domain.vo.req.friend.FriendApplyReq;
import com.gugugu.haochat.user.domain.vo.req.friend.FriendApproveReq;
import com.gugugu.haochat.user.domain.vo.req.friend.FriendCheckReq;
import com.gugugu.haochat.user.domain.vo.resp.friend.FriendApplyResp;
import com.gugugu.haochat.user.domain.vo.resp.friend.FriendCheckResp;
import com.gugugu.haochat.user.domain.vo.resp.friend.FriendResp;
import com.gugugu.haochat.user.domain.vo.resp.friend.FriendUnreadResp;
import com.gugugu.haochat.user.service.FriendService;
import com.gugugu.haochat.common.utils.AssertUtil;
import com.gugugu.haochat.user.service.adapter.FriendAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gugugu.haochat.user.domain.enums.ApplyStatusEnum.WAIT_APPROVAL;

@Service
@Slf4j
public class FriendServiceImpl implements FriendService {
    @Autowired
    private UserFriendDao userFriendDao;
    @Autowired
    private UserApplyDao userApplyDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private RoomService roomService;
    @Autowired
    private ChatService chatService;
    @Override
    public FriendCheckResp check(Long uid, FriendCheckReq req) {
        List<UserFriend> friendList = userFriendDao.getByFriends(uid, req.getUidList());
        Set<Long> friendUidSet = friendList.stream().map(UserFriend::getFriendUid).collect(Collectors.toSet());
        List<FriendCheckResp.FriendCheck> friendCheckList = req.getUidList().stream().map(friendUid -> {
            FriendCheckResp.FriendCheck friendCheck = new FriendCheckResp.FriendCheck();
            friendCheck.setUid(friendUid);
            friendCheck.setIsFriend(friendUidSet.contains(friendUid));
            return friendCheck;
        }).collect(Collectors.toList());
        return new FriendCheckResp(friendCheckList);
    }

    @Override
    @RedissonLock(key = "#uid")
    public void apply(Long uid, FriendApplyReq req) {
        //是否已经是好友了
        UserFriend friend = userFriendDao.getByFriend(uid, req.getTargetUid());
        AssertUtil.isEmpty(friend, "你们已经是好友了");
        //是否有待审批的好友申请记录(自己主动申请)
        UserApply selfApproving = userApplyDao.getFriendApproving(uid, req.getTargetUid());
        if(Objects.nonNull(selfApproving)){
            log.info("已有好友申请记录,uid:{}, targetId:{}", uid, req.getTargetUid());
            return;
        }
        //是否有待审批的好友申请记录(他人申请自己)
        UserApply friendApproving = userApplyDao.getFriendApproving(req.getTargetUid(), uid);
        if(Objects.nonNull(friendApproving)){
            ((FriendService) AopContext.currentProxy()).applyApprove(uid, new FriendApproveReq(friendApproving.getId()));
            return;
        }
        //申请入库
        UserApply insert = FriendAdapter.buildFriendApply(uid, req);
        userApplyDao.save(insert);
        //申请事件
        applicationEventPublisher.publishEvent(new UserApplyEvent(this, insert));
    }

    @Override
    @Transactional
    @RedissonLock(key = "#uid")
    public void applyApprove(Long uid, FriendApproveReq req) {
        UserApply userApply = userApplyDao.getById(req.getApplyId());
        AssertUtil.isNotEmpty(userApply,"申请记录不存在");
        AssertUtil.equal(userApply.getTargetId(),uid,"申请记录不存在");
        AssertUtil.equal(userApply.getStatus(),WAIT_APPROVAL.getCode(),"已同意好友申请");
        //同意申请
        userApplyDao.agree(req.getApplyId());
        //创建双方好友关系
        createFriend(uid, userApply.getUid());
        RoomFriend roomFriend = roomService.createFriendRoom(Arrays.asList(uid, userApply.getUid()));
        //发送一条格式化消息 我们已经是好友了，开始聊天吧
        chatService.sendMsg(MessageAdapter.buildAgreeMsg(roomFriend.getRoomId()),uid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFriend(Long uid, Long friendUid) {
        List<UserFriend> userFriends = userFriendDao.getUserFriend(uid, friendUid);
        if(CollectionUtil.isEmpty(userFriends)){
            log.info("没有好友关系:{},{}",uid, friendUid);
            return;
        }
        List<Long> friendRecordIds = userFriends.stream().map(UserFriend::getId).collect(Collectors.toList());
        userFriendDao.removeByIds(friendRecordIds);
        // 禁用房间
        roomService.disableFriendRoom(Arrays.asList(uid, friendUid));
    }

    @Override
    public PageBaseResp<FriendApplyResp> pageApplyFriend(Long uid, PageBaseReq req) {
        IPage<UserApply> userApplyIPage = userApplyDao.friendApplyPage(uid,req.plusPage());
        if(CollectionUtil.isEmpty(userApplyIPage.getRecords())){
            return PageBaseResp.empty();
        }
        readApples(uid, userApplyIPage);
        return PageBaseResp.init(userApplyIPage, FriendAdapter.buildFriendApplyList(userApplyIPage.getRecords()));

    }

    @Override
    public FriendUnreadResp unread(Long uid) {
        Integer unReadCount = userApplyDao.getUnReadCount(uid);
        return new FriendUnreadResp(unReadCount);
    }

    @Override
    public CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq req) {
        CursorPageBaseResp<UserFriend> friendPage = userFriendDao.getFriendPage(uid, req);
        if(CollectionUtil.isEmpty(friendPage.getList())){
            return CursorPageBaseResp.empty();
        }
        List<Long> friendUids = friendPage.getList()
                .stream().map(UserFriend::getFriendUid)
                .collect(Collectors.toList());
        List<User> userList = userDao.getFriendList(friendUids);
        return CursorPageBaseResp.init(friendPage,FriendAdapter.buildFriend(friendPage.getList(),userList));
    }

    private void readApples(Long uid, IPage<UserApply> userApplyIPage) {
        List<Long> applyIds = userApplyIPage.getRecords()
                .stream().map(UserApply::getId)
                .collect(Collectors.toList());
        userApplyDao.readApples(uid, applyIds);
    }

    private void createFriend(Long uid, Long targetUid) {
        UserFriend userFriend1 = new UserFriend();
        userFriend1.setUid(uid);
        userFriend1.setFriendUid(targetUid);
        UserFriend userFriend2 = new UserFriend();
        userFriend2.setUid(targetUid);
        userFriend2.setFriendUid(uid);
        userFriendDao.saveBatch(Lists.newArrayList(userFriend1,userFriend2));
    }
}
