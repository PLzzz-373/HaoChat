package com.gugugu.haochat.chat.dao;

import com.gugugu.haochat.chat.domain.entity.Room;
import com.gugugu.haochat.chat.mapper.RoomMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 房间表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/PLzzz-373">gugugu</a>
 * @since 2024-03-22
 */
@Service
public class RoomDao extends ServiceImpl<RoomMapper, Room> {

    public void refreshActiveTime(Long roomId, Long msgId, Date msgTime) {
        lambdaUpdate()
                .eq(Room::getId,roomId)
                .lt(Room::getLastMsgId,msgId)
                .set(Room::getLastMsgId, msgId)
                .set(Room::getActiveTime, msgTime)
                .update();
    }
}
