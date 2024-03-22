package com.gugugu.haochat.common.chat.controller;

import com.gugugu.haochat.common.chat.domain.vo.req.ChatMessagePageReq;
import com.gugugu.haochat.common.chat.domain.vo.req.ChatMessageReq;
import com.gugugu.haochat.common.chat.domain.vo.resp.ChatMessageResp;
import com.gugugu.haochat.common.chat.service.ChatService;
import com.gugugu.haochat.common.common.domain.vo.ApiResult;
import com.gugugu.haochat.common.common.domain.vo.req.CursorPageBaseReq;
import com.gugugu.haochat.common.common.domain.vo.resp.CursorPageBaseResp;
import com.gugugu.haochat.common.common.utils.RequestHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/chat")
@Api(tags = "聊天室相关接口")
@Slf4j
public class ChatController {
    @Autowired
    private ChatService chatService;

    @PostMapping("/msg")
    @ApiOperation("发送消息")
    public ApiResult<ChatMessageResp> sendMsg(@Valid @RequestBody ChatMessageReq req){
        Long uid = RequestHolder.get().getUid();
        Long msgId = chatService.sendMsg(req, uid);
        return ApiResult.success(chatService.getMsgResp(msgId, RequestHolder.get().getUid()));
    }
}
