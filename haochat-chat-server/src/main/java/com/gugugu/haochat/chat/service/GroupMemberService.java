package com.gugugu.haochat.chat.service;

import com.gugugu.haochat.chat.domain.vo.member.MemberExitReq;
import com.gugugu.haochat.chat.domain.vo.req.AdminAddReq;
import com.gugugu.haochat.chat.domain.vo.req.AdminRevokeReq;

/**
 * <p>
 * 群成员表 服务类
 * </p>
 *
 * @author <a href="https://github.com/PLzzz-373">gugugu</a>
 * @since 2024-03-22
 */
public interface GroupMemberService {

    void exitGroup(Long uid, MemberExitReq req);

    void addAdmin(Long uid, AdminAddReq req);

    void revokeAdmin(Long uid, AdminRevokeReq req);
}
