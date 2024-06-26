package com.gugugu.haochat.chat.service;

import com.gugugu.haochat.chat.domain.dto.MsgReadInfoDTO;
import com.gugugu.haochat.chat.domain.entity.Message;
import com.gugugu.haochat.chat.domain.vo.member.MemberReq;
import com.gugugu.haochat.chat.domain.vo.req.*;
import com.gugugu.haochat.chat.domain.vo.resp.ChatMemberStatisticResp;
import com.gugugu.haochat.chat.domain.vo.resp.ChatMessageReadResp;
import com.gugugu.haochat.chat.domain.vo.resp.ChatMessageResp;
import com.gugugu.haochat.common.domain.vo.resp.CursorPageBaseResp;
import com.gugugu.haochat.websocket.domain.vo.response.ws.ChatMemberResp;

import java.util.Collection;
import java.util.List;

public interface ChatService {

    Long sendMsg(ChatMessageReq request, Long uid);

    ChatMessageResp getMsgResp(Message message, Long receiveUid);

    ChatMessageResp getMsgResp(Long msgId, Long uid);

    ChatMemberStatisticResp getMemberStatistic();

    void setMsgMark(Long uid, ChatMessageMarkReq req);

    void recallMsg(Long uid, ChatMessageBaseReq request);

    CursorPageBaseResp<ChatMemberResp> getMemberPage(List<Long> memberUidList, MemberReq request);

    CursorPageBaseResp<ChatMessageResp> getMsgPage(ChatMessagePageReq req, Long uid);

    CursorPageBaseResp<ChatMessageReadResp> getReadPage(Long uid, ChatMessageReadReq req);

    Collection<MsgReadInfoDTO> getMsgReadInfo(Long uid, ChatMessageReadInfoReq req);

    void msgRead(Long uid, ChatMessageMemberReq req);
}
