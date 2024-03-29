package com.gugugu.haochat.chat.controller;

import com.gugugu.haochat.chat.domain.vo.req.ChatMessageBaseReq;
import com.gugugu.haochat.chat.domain.vo.req.ChatMessageMarkReq;
import com.gugugu.haochat.chat.domain.vo.req.ChatMessagePageReq;
import com.gugugu.haochat.chat.domain.vo.req.ChatMessageReq;
import com.gugugu.haochat.chat.domain.vo.resp.ChatMessageResp;
import com.gugugu.haochat.chat.service.ChatService;
import com.gugugu.haochat.common.domain.vo.ApiResult;
import com.gugugu.haochat.common.domain.vo.resp.CursorPageBaseResp;
import com.gugugu.haochat.common.utils.RequestHolder;
import com.gugugu.haochat.user.domain.enums.BlackTypeEnum;
import com.gugugu.haochat.user.service.cache.UserCache;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/chat")
@Api(tags = "聊天室相关接口")
@Slf4j
public class ChatController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private UserCache userCache;
    @GetMapping("/public/msg/page")
    @ApiOperation("消息列表")
    public ApiResult<CursorPageBaseResp<ChatMessageResp>> getMsgPage(@Valid ChatMessagePageReq req){
        CursorPageBaseResp<ChatMessageResp> msgPage = chatService.getMsgPage(req,RequestHolder.get().getUid());
        filterBlackMsg(msgPage);
        return ApiResult.success(msgPage);
    }

    private void filterBlackMsg(CursorPageBaseResp<ChatMessageResp> memberPage) {
        Set<String> blackMembers = getBlackUidSet();
        memberPage.getList().removeIf(a -> blackMembers.contains(a.getFromUser().getUid().toString()));
    }

    private Set<String> getBlackUidSet() {
        return userCache.getBlackMap().getOrDefault(BlackTypeEnum.UID.getType(), new HashSet<>());

    }

    @PostMapping("/msg")
    @ApiOperation("发送消息")
    public ApiResult<ChatMessageResp> sendMsg(@Valid @RequestBody ChatMessageReq req){
        Long uid = RequestHolder.get().getUid();
        Long msgId = chatService.sendMsg(req, uid);
        return ApiResult.success(chatService.getMsgResp(msgId, RequestHolder.get().getUid()));
    }

    @PutMapping("/msg/mark")
    @ApiOperation("消息标记")
    public ApiResult<Void> setMsgMark(@Valid @RequestBody ChatMessageMarkReq req){
        chatService.setMsgMark(RequestHolder.get().getUid(), req);
        return ApiResult.success();
    }
    @PutMapping("/msg/recall")
    @ApiOperation("撤回消息")
    public ApiResult<Void> recallMsg(@Valid @RequestBody ChatMessageBaseReq req) {
        chatService.recallMsg(RequestHolder.get().getUid(), req);
        return ApiResult.success();
    }
}
