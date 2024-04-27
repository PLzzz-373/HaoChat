package com.gugugu.haochat.common.emoji.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gugugu.haochat.common.emoji.domain.entity.UserEmoji;
import com.gugugu.haochat.common.emoji.mapper.UserEmojiMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserEmojiDao extends ServiceImpl<UserEmojiMapper, UserEmoji> {
    public List<UserEmoji> listByUid(Long uid) {
        return lambdaQuery().eq(UserEmoji::getUid, uid).list();
    }

    public int countByUid(Long uid) {
        return lambdaQuery().eq(UserEmoji::getUid, uid).count();
    }
}
