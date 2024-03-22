package com.gugugu.haochat.common.user.controller;

import com.gugugu.haochat.common.common.domain.vo.ApiResult;
import com.gugugu.haochat.common.common.domain.vo.req.CursorPageBaseReq;
import com.gugugu.haochat.common.common.domain.vo.req.PageBaseReq;
import com.gugugu.haochat.common.common.domain.vo.resp.CursorPageBaseResp;
import com.gugugu.haochat.common.common.domain.vo.resp.PageBaseResp;
import com.gugugu.haochat.common.user.domain.vo.req.friend.FriendApplyReq;
import com.gugugu.haochat.common.user.domain.vo.req.friend.FriendApproveReq;
import com.gugugu.haochat.common.user.domain.vo.req.friend.FriendCheckReq;
import com.gugugu.haochat.common.user.domain.vo.req.friend.FriendDeleteReq;
import com.gugugu.haochat.common.user.domain.vo.resp.friend.FriendApplyResp;
import com.gugugu.haochat.common.user.domain.vo.resp.friend.FriendCheckResp;
import com.gugugu.haochat.common.user.domain.vo.resp.friend.FriendResp;
import com.gugugu.haochat.common.user.domain.vo.resp.friend.FriendUnreadResp;
import com.gugugu.haochat.common.user.service.FriendService;
import com.gugugu.haochat.common.common.utils.RequestHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/user/friend")
@Api(tags = "好友相关接口")
@Slf4j
public class FriendController {
    @Resource
    private FriendService friendService;

    @GetMapping("/check")
    @ApiOperation("批量判断是否是自己好友")
    public ApiResult<FriendCheckResp> check(@Valid FriendCheckReq req){
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.check(uid,req));
    }

    @PostMapping("/apply")
    @ApiOperation("申请好友")
    public ApiResult<Void> apply(@Valid @RequestBody FriendApplyReq req){
        Long uid = RequestHolder.get().getUid();
        friendService.apply(uid, req);
        return ApiResult.success();
    }

    @DeleteMapping()
    @ApiOperation("删除好友")
    public ApiResult<Void> delete(@Valid @RequestBody FriendDeleteReq req){
        Long uid = RequestHolder.get().getUid();
        friendService.deleteFriend(uid, req.getTargetUid());
        return ApiResult.success();
    }

    @GetMapping("/apply/page")
    @ApiOperation("好友申请列表")
    public ApiResult<PageBaseResp<FriendApplyResp>> page(@Valid PageBaseReq req){
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.pageApplyFriend(uid,req));

    }

    @GetMapping("/apply/unread")
    @ApiOperation("申请未读数")
    public ApiResult<FriendUnreadResp> unread(){
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.unread(uid));
    }

    @PutMapping("/apply")
    @ApiOperation("审批同意")
    public ApiResult<Void> applyApprove(@Valid @RequestBody FriendApproveReq req){
        friendService.applyApprove(RequestHolder.get().getUid(), req);
        return ApiResult.success();
    }

    @GetMapping("/page")
    @ApiOperation("联系人列表")
    public ApiResult<CursorPageBaseResp<FriendResp>> friendList(@Valid CursorPageBaseReq req){
        Long uid = RequestHolder.get().getUid();
        return ApiResult.success(friendService.friendList(uid, req));
    }

}
