package com.gugugu.haochat.common.chat.dao;

import com.gugugu.haochat.common.chat.domain.entity.Contact;
import com.gugugu.haochat.common.chat.mapper.ContactMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会话列表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/PLzzz-373">gugugu</a>
 * @since 2024-03-22
 */
@Service
public class ContactDao extends ServiceImpl<ContactMapper, Contact>  {

    public Contact get(Long recUid, Long roomId) {
        return lambdaQuery()
                .eq(Contact::getUid, recUid)
                .eq(Contact::getRoomId, roomId)
                .one();
    }
}
