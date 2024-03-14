package com.gugugu.haochat.common.user.dao;

import com.gugugu.haochat.common.user.domain.entity.User;
import com.gugugu.haochat.common.user.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2024-03-12
 */
@Service
public class UserDao extends ServiceImpl<UserMapper, User>  {

    public User getByOpenId(String openId) {
        return lambdaQuery()
                .eq(User::getOpenId,openId)
                .one();
    }
}
