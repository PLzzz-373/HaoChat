package com.gugugu.haochat.user.service.impl;

import com.gugugu.haochat.user.domain.enums.RoleEnum;
import com.gugugu.haochat.user.service.IRoleService;
import com.gugugu.haochat.user.service.cache.UserCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
@Service
public class RoleServiceImpl implements IRoleService {
    @Autowired
    private UserCache userCache;

    @Override
    public boolean hasPower(Long uid, RoleEnum roleEnum) {//超级管理员
        Set<Long> roleSet = userCache.getRoleSet(uid);
        return isAdmin(roleSet) || roleSet.contains(roleEnum.getId());
    }

    private boolean isAdmin(Set<Long> roleSet) {
        return Objects.requireNonNull(roleSet).contains(RoleEnum.ADMIN.getId());
    }
}
