package com.gugugu.haochat.chat.controller;


import com.gugugu.haochat.chat.domain.vo.member.MemberAddReq;
import com.gugugu.haochat.chat.domain.vo.member.MemberDelReq;
import com.gugugu.haochat.chat.domain.vo.member.MemberExitReq;
import com.gugugu.haochat.chat.domain.vo.member.MemberReq;
import com.gugugu.haochat.chat.domain.vo.req.AdminAddReq;
import com.gugugu.haochat.chat.domain.vo.req.AdminRevokeReq;
import com.gugugu.haochat.chat.domain.vo.req.ChatMessageMemberReq;
import com.gugugu.haochat.chat.domain.vo.req.GroupAddReq;
import com.gugugu.haochat.chat.domain.vo.resp.ChatMemberListResp;
import com.gugugu.haochat.chat.domain.vo.resp.MemberResp;
import com.gugugu.haochat.chat.service.GroupMemberService;
import com.gugugu.haochat.chat.service.RoomAppService;
import com.gugugu.haochat.common.domain.vo.ApiResult;
import com.gugugu.haochat.common.domain.vo.req.IdReqVO;
import com.gugugu.haochat.common.domain.vo.resp.CursorPageBaseResp;
import com.gugugu.haochat.common.domain.vo.resp.IdRespVO;
import com.gugugu.haochat.common.utils.RequestHolder;
import com.gugugu.haochat.websocket.domain.vo.response.ws.ChatMemberResp;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 房间表 前端控制器
 * </p>
 *
 * @author <a href="https://github.com/PLzzz-373">gugugu</a>
 * @since 2024-03-22
 */
@RestController
@RequestMapping("/api/room")
@Api(tags = "群组相关接口")
public class RoomController {
    @Autowired
    private RoomAppService roomService;
    @Autowired
    private GroupMemberService groupMemberService;

    @GetMapping("/public/group")
    @ApiOperation("群组详情")
    public ApiResult<MemberResp> groupDetail(@Valid IdReqVO req) {
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(roomService.getGroupDetail(uid, req.getId()));
    }

    @GetMapping("/public/group/member/page")
    @ApiOperation("群成员列表")
    public ApiResult<CursorPageBaseResp<ChatMemberResp>> getMemberPage(@Valid MemberReq req) {
        return ApiResult.success(roomService.getMemberPage(req));
    }

    @GetMapping("/group/member/list")
    @ApiOperation("房间内的所有群成员列表-@专用")
    public ApiResult<List<ChatMemberListResp>> getMemberList(@Valid ChatMessageMemberReq req) {
        return ApiResult.success(roomService.getMemberList(req));
    }

    @DeleteMapping("/group/member")
    @ApiOperation("移除成员")
    public ApiResult<Void> delMember(@Valid @RequestBody MemberDelReq req) {
        Long uid = RequestHolder.get().getUid();
        roomService.delMember(uid, req);
        return ApiResult.success();
    }

    @DeleteMapping("/group/member/exit")
    @ApiOperation("退出群聊")
    public ApiResult<Boolean> exitGroup(@Valid @RequestBody MemberExitReq req) {
        Long uid = RequestHolder.get().getUid();
        groupMemberService.exitGroup(uid, req);
        return ApiResult.success();
    }

    @PostMapping("/group")
    @ApiOperation("新增群组")
    public ApiResult<IdRespVO> addGroup(@Valid @RequestBody GroupAddReq req) {
        Long uid = RequestHolder.get().getUid();
        Long roomId = roomService.addGroup(uid, req);
        return ApiResult.success(IdRespVO.id(roomId));
    }

    @PostMapping("/group/member")
    @ApiOperation("邀请好友")
    public ApiResult<Void> addMember(@Valid @RequestBody MemberAddReq req) {
        Long uid = RequestHolder.get().getUid();
        roomService.addMember(uid, req);
        return ApiResult.success();
    }

    @PutMapping("/group/admin")
    @ApiOperation("添加管理员")
    public ApiResult<Boolean> addAdmin(@Valid @RequestBody AdminAddReq req){
        Long uid = RequestHolder.get().getUid();
        groupMemberService.addAdmin(uid, req);
        return ApiResult.success();
    }

    @DeleteMapping("/group/admin")
    @ApiOperation("撤销管理员")
    public ApiResult<Boolean> revokeAdmin(@Valid @RequestBody AdminRevokeReq req){
        Long uid = RequestHolder.get().getUid();
        groupMemberService.revokeAdmin(uid, req);
        return ApiResult.success();
    }
}

