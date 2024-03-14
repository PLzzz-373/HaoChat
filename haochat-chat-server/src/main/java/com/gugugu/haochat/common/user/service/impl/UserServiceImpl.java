package com.gugugu.haochat.common.user.service.impl;

import com.gugugu.haochat.common.user.dao.UserDao;
import com.gugugu.haochat.common.user.domain.entity.User;
import com.gugugu.haochat.common.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Override
    @Transactional
    public Long register(User insert) {
        userDao.save(insert);
        //todo 用户注册事件
        return insert.getId();
    }
}
