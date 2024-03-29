package com.gugugu.haochat.chat.dao;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gugugu.haochat.chat.domain.entity.Contact;
import com.gugugu.haochat.chat.mapper.ContactMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gugugu.haochat.common.domain.vo.req.CursorPageBaseReq;
import com.gugugu.haochat.common.domain.vo.resp.CursorPageBaseResp;
import com.gugugu.haochat.common.utils.CursorUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

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

    public void refreshOrCreateActiveTime(Long roomId, List<Long> memberUidList, Long msgId, Date activeTime) {
        baseMapper.refreshOrCreateActiveTime(roomId, memberUidList, msgId, activeTime);
    }

    public CursorPageBaseResp<Contact> getContactPage(Long uid, CursorPageBaseReq req) {
        return CursorUtils.getCursorPageByMysql(this, req, wrapper -> {
            wrapper.eq(Contact::getUid, uid);
        }, Contact::getActiveTime);
    }

    public List<Contact> getByRoomIds(List<Long> roomIds, Long uid) {
        return lambdaQuery()
                .in(Contact::getRoomId, roomIds)
                .eq(Contact::getUid, uid)
                .list();
    }

    public Boolean removeByRoomId(Long roomId, List<Long> uidList) {
        if (CollectionUtil.isNotEmpty(uidList)) {
            LambdaQueryWrapper<Contact> wrapper = new QueryWrapper<Contact>().lambda()
                    .eq(Contact::getRoomId, roomId)
                    .in(Contact::getUid, uidList);
            return this.remove(wrapper);
        }
        return false;
    }
}
