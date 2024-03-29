package com.gugugu.haochat.chat.controller;


import com.gugugu.haochat.chat.domain.vo.req.ContactFriendReq;
import com.gugugu.haochat.chat.domain.vo.resp.ChatRoomResp;
import com.gugugu.haochat.chat.service.RoomAppService;
import com.gugugu.haochat.common.domain.vo.ApiResult;
import com.gugugu.haochat.common.domain.vo.req.CursorPageBaseReq;
import com.gugugu.haochat.common.domain.vo.req.IdReqVO;
import com.gugugu.haochat.common.domain.vo.resp.CursorPageBaseResp;
import com.gugugu.haochat.common.utils.RequestHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>
 * 会话列表 前端控制器
 * </p>
 *
 * @author <a href="https://github.com/PLzzz-373">gugugu</a>
 * @since 2024-03-22
 */
@RestController
@RequestMapping("/api/chat")
@Api(tags = "会话相关接口")
@Slf4j
public class ContactController {

    @Autowired
    private RoomAppService roomService;
    @GetMapping("/public/contact/page")
    @ApiOperation("会话列表")
    public ApiResult<CursorPageBaseResp<ChatRoomResp>> getRoomPage(@Valid CursorPageBaseReq req) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(roomService.getContactPage(req, uid));
    }

    @GetMapping("/public/contact/detail")
    @ApiOperation("会话详情")
    public ApiResult<ChatRoomResp> getContactDetail(@Valid IdReqVO req) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(roomService.getContactDetail(uid, req.getId()));
    }

    @GetMapping("/public/contact/detail/friend")
    @ApiOperation("会话详情(联系人列表发消息用)")
    public ApiResult<ChatRoomResp> getContactDetailByFriend(@Valid ContactFriendReq request) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(roomService.getContactDetailByFriend(uid, request.getUid()));
    }
}

