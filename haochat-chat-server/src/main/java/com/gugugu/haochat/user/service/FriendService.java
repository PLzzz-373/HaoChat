package com.gugugu.haochat.user.service;

import com.gugugu.haochat.common.domain.vo.req.CursorPageBaseReq;
import com.gugugu.haochat.common.domain.vo.req.PageBaseReq;
import com.gugugu.haochat.common.domain.vo.resp.CursorPageBaseResp;
import com.gugugu.haochat.common.domain.vo.resp.PageBaseResp;
import com.gugugu.haochat.user.domain.vo.req.friend.FriendApplyReq;
import com.gugugu.haochat.user.domain.vo.req.friend.FriendApproveReq;
import com.gugugu.haochat.user.domain.vo.req.friend.FriendCheckReq;
import com.gugugu.haochat.user.domain.vo.resp.friend.FriendApplyResp;
import com.gugugu.haochat.user.domain.vo.resp.friend.FriendCheckResp;
import com.gugugu.haochat.user.domain.vo.resp.friend.FriendResp;
import com.gugugu.haochat.user.domain.vo.resp.friend.FriendUnreadResp;

public interface FriendService {
    FriendCheckResp check(Long uid, FriendCheckReq req);

    void apply(Long uid, FriendApplyReq req);

    void applyApprove(Long uid, FriendApproveReq friendApproveReq);

    void deleteFriend(Long uid, Long friendUid);

    PageBaseResp<FriendApplyResp> pageApplyFriend(Long uid, PageBaseReq req);

    FriendUnreadResp unread(Long uid);

    CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq req);
}
