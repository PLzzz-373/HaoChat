package com.gugugu.haochat.common.user.service.impl;

import com.gugugu.haochat.common.exception.BusinessException;
import com.gugugu.haochat.common.user.dao.UserBackpackDao;
import com.gugugu.haochat.common.user.dao.UserDao;
import com.gugugu.haochat.common.user.domain.entity.User;
import com.gugugu.haochat.common.user.domain.entity.UserBackpack;
import com.gugugu.haochat.common.user.domain.enums.ItemEnum;
import com.gugugu.haochat.common.user.domain.vo.resp.UserInfoResp;
import com.gugugu.haochat.common.user.service.UserService;
import com.gugugu.haochat.common.user.service.adapter.UserAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserBackpackDao userBackpackDao;

    @Override
    @Transactional
    public Long register(User insert) {
        userDao.save(insert);
        //todo 用户注册事件
        return insert.getId();
    }

    @Override
    public UserInfoResp getUserInfo(Long uid) {
        User user = userDao.getById(uid);
        Integer modifyNameCount = userBackpackDao.getCountByValidItemId(uid, ItemEnum.MODIFY_NAME_CARD.getId());

        return UserAdapter.buildUserInfo(user, modifyNameCount);
    }

    @Override
    public void modifyName(Long uid, String name) {
        User oldUser = userDao.getByName(name);
        if(Objects.nonNull(oldUser)){
            throw new BusinessException("用户名重复，请更换用户名");
        }

    }
}
