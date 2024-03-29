package com.gugugu.haochat.user.dao;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.gugugu.haochat.common.domain.enums.NormalOrNoEnum;
import com.gugugu.haochat.common.domain.vo.req.CursorPageBaseReq;
import com.gugugu.haochat.common.domain.vo.resp.CursorPageBaseResp;
import com.gugugu.haochat.common.utils.CursorUtils;
import com.gugugu.haochat.user.domain.entity.User;
import com.gugugu.haochat.user.domain.enums.ChatActiveStatusEnum;
import com.gugugu.haochat.user.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserDao extends ServiceImpl<UserMapper, User>  {

    public User getByOpenId(String openId) {
        return lambdaQuery()
                .eq(User::getOpenId,openId)
                .one();
    }

    public User getByName(String name) {
        return lambdaQuery()
                .eq(User::getName, name)
                .one();
    }

    public void modifyName(Long uid, String name) {
        lambdaUpdate()
                .eq(User::getId, uid)
                .set(User::getName, name)
                .update();


    }

    public void wearingBadge(Long uid, Long itemId) {
        lambdaUpdate()
                .eq(User::getId,uid)
                .set(User::getItemId,itemId)
                .update();
    }

    public List<User> getFriendList(List<Long> friendUids) {
        return lambdaQuery()
                .in(User::getId, friendUids)
                .select(User::getId, User::getActiveStatus, User::getName, User::getAvatar)
                .list();
    }

    public Number getOnlineCount(List<Long> memberUidList) {
        return lambdaQuery()
                .eq(User::getActiveStatus, ChatActiveStatusEnum.OFFLINE.getStatus())
                .in(CollectionUtil.isNotEmpty(memberUidList), User::getId, memberUidList)
                .count();
    }

    public CursorPageBaseResp<User> getCursorPage(List<Long> memberUidList, CursorPageBaseReq req, ChatActiveStatusEnum online) {
        return CursorUtils.getCursorPageByMysql(this, req, wrapper -> {
            wrapper.eq(User::getActiveStatus, online.getStatus());//筛选上线或者离线的
            wrapper.in(CollectionUtils.isNotEmpty(memberUidList), User::getId, memberUidList);//普通群对uid列表做限制
        }, User::getLastOptTime);
    }

    public List<User> getMemberList() {
        return lambdaQuery()
                .eq(User::getStatus, NormalOrNoEnum.NORMAL.getStatus())
                .orderByDesc(User::getLastOptTime)//最近活跃的1000个人，可以用lastOptTime字段，但是该字段没索引，updateTime可平替
                .last("limit 1000")//毕竟是大群聊，人数需要做个限制
                .select(User::getId, User::getName, User::getAvatar)
                .list();
    }
}
