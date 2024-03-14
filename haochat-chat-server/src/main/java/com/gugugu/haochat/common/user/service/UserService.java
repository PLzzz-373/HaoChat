package com.gugugu.haochat.common.user.service;

import com.gugugu.haochat.common.user.domain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author <a href="https://github.com/zongzibinbin">abin</a>
 * @since 2024-03-12
 */
public interface UserService{

    Long register(User insert);
}
