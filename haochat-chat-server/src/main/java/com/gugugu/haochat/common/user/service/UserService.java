package com.gugugu.haochat.common.user.service;

import com.gugugu.haochat.common.user.domain.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gugugu.haochat.common.user.domain.vo.resp.BadgeResp;
import com.gugugu.haochat.common.user.domain.vo.resp.UserInfoResp;

import java.util.List;


public interface UserService{

    Long register(User insert);

    UserInfoResp getUserInfo(Long uid);

    void modifyName(Long uid, String name);

    List<BadgeResp> badges(Long uid);

    void wearingBadge(Long uid, Long itemId);
}
