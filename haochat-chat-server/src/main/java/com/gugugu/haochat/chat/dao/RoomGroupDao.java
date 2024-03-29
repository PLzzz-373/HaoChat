package com.gugugu.haochat.chat.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gugugu.haochat.chat.domain.entity.RoomGroup;
import com.gugugu.haochat.chat.mapper.RoomGroupMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomGroupDao extends ServiceImpl<RoomGroupMapper, RoomGroup> {

    public List<RoomGroup> listByRoomIds(List<Long> roomIds) {
        return lambdaQuery()
                .in(RoomGroup::getRoomId, roomIds)
                .list();
    }

    public RoomGroup getByRoomId(Long roomId) {
        return lambdaQuery()
                .eq(RoomGroup::getRoomId, roomId)
                .one();
    }
}