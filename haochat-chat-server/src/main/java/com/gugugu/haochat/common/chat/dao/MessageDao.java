package com.gugugu.haochat.common.chat.dao;

import com.gugugu.haochat.common.chat.domain.entity.Message;
import com.gugugu.haochat.common.chat.domain.enums.MessageStatusEnum;
import com.gugugu.haochat.common.chat.domain.vo.req.ChatMessagePageReq;
import com.gugugu.haochat.common.chat.mapper.MessageMapper;
import com.gugugu.haochat.common.chat.service.IMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gugugu.haochat.common.common.domain.vo.resp.CursorPageBaseResp;
import com.gugugu.haochat.common.common.utils.CursorUtils;
import org.springframework.stereotype.Service;

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

    public CursorPageBaseResp<Message> getCursorPage(Long roomId, ChatMessagePageReq req, Long lastMsgId) {
        return CursorUtils.getCursorPageByMysql(this, req, wrapper -> {
            wrapper.eq(Message::getRoomId, roomId);
            wrapper.eq(Message::getStatus, MessageStatusEnum.NORMAL.getStatus());
            wrapper.le(Objects.nonNull(lastMsgId), Message::getId, lastMsgId);
        }, Message::getId);
    }
}
