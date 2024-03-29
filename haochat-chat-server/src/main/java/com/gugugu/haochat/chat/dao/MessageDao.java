package com.gugugu.haochat.chat.dao;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gugugu.haochat.chat.domain.entity.Message;
import com.gugugu.haochat.chat.domain.enums.MessageStatusEnum;
import com.gugugu.haochat.chat.domain.vo.req.ChatMessagePageReq;
import com.gugugu.haochat.chat.mapper.MessageMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gugugu.haochat.common.domain.vo.req.CursorPageBaseReq;
import com.gugugu.haochat.common.domain.vo.resp.CursorPageBaseResp;
import com.gugugu.haochat.common.utils.CursorUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 消息表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/PLzzz-373">gugugu</a>
 * @since 2024-03-22
 */
@Service
public class MessageDao extends ServiceImpl<MessageMapper, Message>  {

    public CursorPageBaseResp<Message> getCursorPage(Long roomId, CursorPageBaseReq req, Long lastMsgId) {
        return CursorUtils.getCursorPageByMysql(this, req, wrapper -> {
            wrapper.eq(Message::getRoomId, roomId);
            wrapper.eq(Message::getStatus, MessageStatusEnum.NORMAL.getStatus());
            wrapper.le(Objects.nonNull(lastMsgId), Message::getId, lastMsgId);
        }, Message::getId);
    }

    public Integer getUnReadCount(Long roomId, Date readTime) {
        return lambdaQuery()
                .eq(Message::getRoomId, roomId)
                .gt(Objects.nonNull(readTime), Message::getCreateTime, readTime)
                .count();
    }

    public Boolean removeByRoomId(Long roomId, List<Long> uidList) {
        if (CollectionUtil.isNotEmpty(uidList)) {
            LambdaUpdateWrapper<Message> wrapper = new UpdateWrapper<Message>().lambda()
                    .eq(Message::getRoomId, roomId)
                    .in(Message::getFromUid, uidList)
                    .set(Message::getStatus, MessageStatusEnum.DELETE.getStatus());
            return this.update(wrapper);
        }
        return false;
    }

    public void invalidByUid(Long uid) {
        lambdaUpdate()
                .eq(Message::getFromUid,uid)
                .set(Message::getStatus,MessageStatusEnum.DELETE.getStatus())
                .update();
    }
}
