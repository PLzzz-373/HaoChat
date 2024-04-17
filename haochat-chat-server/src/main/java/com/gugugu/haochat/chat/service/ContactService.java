package com.gugugu.haochat.chat.service;

import com.gugugu.haochat.chat.domain.dto.MsgReadInfoDTO;
import com.gugugu.haochat.chat.domain.entity.Message;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 会话列表 服务类
 * </p>
 *
 * @author <a href="https://github.com/PLzzz-373">gugugu</a>
 * @since 2024-03-22
 */
public interface ContactService {

    Map<Long, MsgReadInfoDTO> getMsgReadInfo(List<Message> messages);
}
