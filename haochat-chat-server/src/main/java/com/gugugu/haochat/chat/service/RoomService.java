package com.gugugu.haochat.chat.service;

import com.gugugu.haochat.chat.domain.entity.RoomFriend;
import com.gugugu.haochat.chat.domain.entity.RoomGroup;

import java.util.List;

/**
 * <p>
 * 房间表 服务类
 * </p>
 *
 * @author <a href="https://github.com/PLzzz-373">gugugu</a>
 * @since 2024-03-22
 */
public interface RoomService {

    RoomFriend getFriendRoom(Long uid, Long friendUid);

    RoomGroup createGroupRoom(Long uid);

    RoomFriend createFriendRoom(List<Long> list);

    void disableFriendRoom(List<Long> list);
}
