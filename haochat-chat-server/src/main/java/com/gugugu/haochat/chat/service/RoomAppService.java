package com.gugugu.haochat.chat.service;

import com.gugugu.haochat.chat.domain.vo.member.MemberAddReq;
import com.gugugu.haochat.chat.domain.vo.member.MemberDelReq;
import com.gugugu.haochat.chat.domain.vo.member.MemberReq;
import com.gugugu.haochat.chat.domain.vo.req.ChatMessageMemberReq;
import com.gugugu.haochat.chat.domain.vo.req.GroupAddReq;
import com.gugugu.haochat.chat.domain.vo.resp.ChatMemberListResp;
import com.gugugu.haochat.chat.domain.vo.resp.ChatRoomResp;
import com.gugugu.haochat.chat.domain.vo.resp.MemberResp;
import com.gugugu.haochat.common.domain.vo.req.CursorPageBaseReq;
import com.gugugu.haochat.common.domain.vo.resp.CursorPageBaseResp;
import com.gugugu.haochat.websocket.domain.vo.response.ws.ChatMemberResp;

import java.util.List;

public interface RoomAppService {
    /**
     * 获取会话列表--支持未登录态
     */
    CursorPageBaseResp<ChatRoomResp> getContactPage(CursorPageBaseReq request, Long uid);

    /**
     * 获取群组信息
     */
    MemberResp getGroupDetail(Long uid, long roomId);

    CursorPageBaseResp<ChatMemberResp> getMemberPage(MemberReq request);

    List<ChatMemberListResp> getMemberList(ChatMessageMemberReq request);

    void delMember(Long uid, MemberDelReq request);

    void addMember(Long uid, MemberAddReq request);

    Long addGroup(Long uid, GroupAddReq request);

    ChatRoomResp getContactDetail(Long uid, Long roomId);

    ChatRoomResp getContactDetailByFriend(Long uid, Long friendUid);
}
