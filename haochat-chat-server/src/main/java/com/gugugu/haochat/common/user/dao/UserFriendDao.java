package com.gugugu.haochat.common.user.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gugugu.haochat.common.common.domain.vo.req.CursorPageBaseReq;
import com.gugugu.haochat.common.common.domain.vo.resp.CursorPageBaseResp;
import com.gugugu.haochat.common.common.utils.CursorUtils;
import com.gugugu.haochat.common.user.domain.entity.UserFriend;
import com.gugugu.haochat.common.user.mapper.UserFriendMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 用户联系人表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/PLzzz-373">gugugu</a>
 * @since 2024-03-22
 */
@Service
public class UserFriendDao extends ServiceImpl<UserFriendMapper, UserFriend>  {

    public List<UserFriend> getByFriends(Long uid, List<Long> uidList) {
        return lambdaQuery()
                .eq(UserFriend::getUid, uid)
                .in(UserFriend::getFriendUid, uidList)
                .list();
    }

    public UserFriend getByFriend(Long uid, Long targetUid) {
        return lambdaQuery()
                .eq(UserFriend::getUid, uid)
                .eq(UserFriend::getFriendUid, targetUid)
                .one();
    }

    public List<UserFriend> getUserFriend(Long uid, Long friendUid) {
        return lambdaQuery()
                .eq(UserFriend::getUid, uid)
                .eq(UserFriend::getFriendUid, friendUid)
                .or()
                .eq(UserFriend::getFriendUid, uid)
                .eq(UserFriend::getUid, friendUid)
                .select(UserFriend::getId)
                .list();
    }

    public CursorPageBaseResp<UserFriend> getFriendPage(Long uid, CursorPageBaseReq req) {
        return CursorUtils.getCursorPageByMysql(this, req,
                wrapper -> wrapper.eq(UserFriend::getUid, uid), UserFriend::getId);
    }
}
