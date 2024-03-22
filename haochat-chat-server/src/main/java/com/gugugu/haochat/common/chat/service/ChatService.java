package com.gugugu.haochat.common.chat.service;

import com.gugugu.haochat.common.chat.domain.entity.Message;
import com.gugugu.haochat.common.chat.domain.vo.req.ChatMessagePageReq;
import com.gugugu.haochat.common.chat.domain.vo.req.ChatMessageReq;
import com.gugugu.haochat.common.chat.domain.vo.resp.ChatMessageResp;
import com.gugugu.haochat.common.common.domain.vo.ApiResult;
import com.gugugu.haochat.common.common.domain.vo.resp.CursorPageBaseResp;

public interface ChatService {

    Long sendMsg(ChatMessageReq request, Long uid);

    ChatMessageResp getMsgResp(Message message, Long receiveUid);

    ChatMessageResp getMsgResp(Long msgId, Long uid);
}
