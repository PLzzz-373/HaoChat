package com.gugugu.haochat.chat.service.impl;

import com.gugugu.haochat.chat.dao.*;
import com.gugugu.haochat.chat.domain.entity.Room;
import com.gugugu.haochat.chat.domain.entity.RoomGroup;
import com.gugugu.haochat.chat.domain.vo.member.MemberExitReq;
import com.gugugu.haochat.chat.domain.vo.req.AdminAddReq;
import com.gugugu.haochat.chat.domain.vo.req.AdminRevokeReq;
import com.gugugu.haochat.chat.service.GroupMemberService;
import com.gugugu.haochat.chat.service.adapter.MemberAdapter;
import com.gugugu.haochat.chat.service.cache.GroupMemberCache;
import com.gugugu.haochat.common.exception.CommonErrorEnum;
import com.gugugu.haochat.common.exception.GroupErrorEnum;
import com.gugugu.haochat.common.utils.AssertUtil;
import com.gugugu.haochat.user.service.impl.PushService;
import com.gugugu.haochat.websocket.domain.vo.response.ws.WSBaseResp;
import com.gugugu.haochat.websocket.domain.vo.response.ws.WSMemberChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.gugugu.haochat.chat.constant.GroupConst.MAX_MANAGE_COUNT;

@Service
public class GroupMemberServiceImpl implements GroupMemberService {
    @Autowired
    private RoomGroupDao roomGroupDao;
    @Autowired
    private RoomDao roomDao;
    @Autowired
    private GroupMemberDao groupMemberDao;
    @Autowired
    private ContactDao contactDao;
    @Autowired
    private MessageDao messageDao;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private PushService pushService;
    @Override
    public void exitGroup(Long uid, MemberExitReq req) {
        Long roomId = req.getRoomId();
        // 1. 判断群聊是否存在
        RoomGroup roomGroup = roomGroupDao.getByRoomId(roomId);
        AssertUtil.isNotEmpty(roomGroup, GroupErrorEnum.GROUP_NOT_EXIST);

        // 2. 判断房间是否是大群聊 （大群聊禁止退出）
        Room room = roomDao.getById(roomId);
        AssertUtil.isFalse(room.isHotRoom(), GroupErrorEnum.NOT_ALLOWED_FOR_EXIT_GROUP);

        // 3. 判断群成员是否在群中
        Boolean isGroupShip = groupMemberDao.isGroupShip(roomGroup.getRoomId(), Collections.singletonList(uid));
        AssertUtil.isTrue(isGroupShip, GroupErrorEnum.USER_NOT_IN_GROUP);

        // 4. 判断该用户是否是群主
        Boolean isLord = groupMemberDao.isLord(roomGroup.getId(), uid);
        if (isLord) {
            //找出该群组所有成员
            List<Long> memberUidList = groupMemberDao.getMemberUidList(roomGroup.getId());
            for(Long member:memberUidList){
                System.out.println(member);
            }
            // 4.1 删除房间
            boolean isDelRoom = roomDao.removeById(roomId);
            AssertUtil.isTrue(isDelRoom, CommonErrorEnum.SYSTEM_ERROR);
            // 4.2 删除会话
            Boolean isDelContact = contactDao.removeByRoomId(roomId, memberUidList);
            System.out.println(isDelContact);
            AssertUtil.isTrue(isDelContact, CommonErrorEnum.SYSTEM_ERROR);
            // 4.3 删除群成员
            Boolean isDelGroupMember = groupMemberDao.removeByGroupId(roomGroup.getId(), memberUidList);
            AssertUtil.isTrue(isDelGroupMember, CommonErrorEnum.SYSTEM_ERROR);
            // 4.4 删除消息记录 (逻辑删除)
            Boolean isDelMessage = messageDao.removeByRoomId(roomId, memberUidList);
            AssertUtil.isTrue(isDelMessage, CommonErrorEnum.SYSTEM_ERROR);
        } else {
            // 4.5 删除会话
            Boolean isDelContact = contactDao.removeByRoomId(roomId, Collections.singletonList(uid));
            AssertUtil.isTrue(isDelContact, CommonErrorEnum.SYSTEM_ERROR);
            // 4.6 删除群成员
            Boolean isDelGroupMember = groupMemberDao.removeByGroupId(roomGroup.getId(), Collections.singletonList(uid));
            AssertUtil.isTrue(isDelGroupMember, CommonErrorEnum.SYSTEM_ERROR);
            // 4.7 发送移除事件告知群成员
            List<Long> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
            WSBaseResp<WSMemberChange> ws = MemberAdapter.buildMemberRemoveWS(roomGroup.getRoomId(), uid);
            pushService.sendPushMsg(ws, memberUidList);
            groupMemberCache.evictMemberUidList(room.getId());
        }
    }

    @Override
    public void addAdmin(Long uid, AdminAddReq req) {
        // 1. 判断群聊是否存在
        RoomGroup roomGroup = roomGroupDao.getByRoomId(req.getRoomId());
        AssertUtil.isNotEmpty(roomGroup, GroupErrorEnum.GROUP_NOT_EXIST);

        // 2. 判断该用户是否是群主
        Boolean isLord = groupMemberDao.isLord(roomGroup.getId(), uid);
        AssertUtil.isTrue(isLord, GroupErrorEnum.NOT_ALLOWED_OPERATION);

        // 3. 判断群成员是否在群中
        Boolean isGroupShip = groupMemberDao.isGroupShip(roomGroup.getRoomId(), req.getUidList());
        AssertUtil.isTrue(isGroupShip, GroupErrorEnum.USER_NOT_IN_GROUP);

        // 4. 判断管理员数量是否达到上限
        // 4.1 查询现有管理员数量
        List<Long> manageUidList = groupMemberDao.getManageUidList(roomGroup.getId());
        // 4.2 去重
        HashSet<Long> manageUidSet = new HashSet<>(manageUidList);
        manageUidSet.addAll(req.getUidList());
        AssertUtil.isFalse(manageUidSet.size() > MAX_MANAGE_COUNT, GroupErrorEnum.MANAGE_COUNT_EXCEED);

        // 5. 增加管理员
        groupMemberDao.addAdmin(roomGroup.getId(), req.getUidList());
    }

    @Override
    public void revokeAdmin(Long uid, AdminRevokeReq req) {
        // 1. 判断群聊是否存在
        RoomGroup roomGroup = roomGroupDao.getByRoomId(req.getRoomId());
        AssertUtil.isNotEmpty(roomGroup, GroupErrorEnum.GROUP_NOT_EXIST);

        // 2. 判断该用户是否是群主
        Boolean isLord = groupMemberDao.isLord(roomGroup.getId(), uid);
        AssertUtil.isTrue(isLord, GroupErrorEnum.NOT_ALLOWED_OPERATION);

        // 3. 判断群成员是否在群中
        Boolean isGroupShip = groupMemberDao.isGroupShip(roomGroup.getRoomId(), req.getUidList());
        AssertUtil.isTrue(isGroupShip, GroupErrorEnum.USER_NOT_IN_GROUP);

        // 4. 撤销管理员
        groupMemberDao.revokeAdmin(roomGroup.getId(), req.getUidList());
    }
}
